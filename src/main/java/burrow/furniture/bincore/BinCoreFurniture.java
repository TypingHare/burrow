package burrow.furniture.bincore;

import burrow.core.Burrow;
import burrow.core.chamber.Chamber;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.List;

@BurrowFurniture(
    simpleName = "Bin Core",
    description = "Bin Core allows developers to create shell files.",
    type = BurrowFurniture.Type.COMPONENT
)
public class BinCoreFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Bin Core";
    public static final String DEFAULT_BIN_SHELL = "/bin/zsh";
    public static final String BIN_NAME_PREFIX = "b";
    private static final Logger logger = LoggerFactory.getLogger(BinCoreFurniture.class);

    public BinCoreFurniture(@NotNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public Collection<String> configKeys() {
        return List.of(ConfigKey.BIN_SHELL, ConfigKey.BIN_NAME);
    }

    @Override
    public void initializeConfig(@NotNull final Config config) {
        config.setIfAbsent(ConfigKey.BIN_SHELL, DEFAULT_BIN_SHELL);
        config.setIfAbsent(ConfigKey.BIN_NAME, BIN_NAME_PREFIX + chamber.getName());
    }

    @Override
    public void beforeInitialization() {
        registerCommand(CreateShellCommand.class);
    }

    /**
     * Creates a shell file in the bin directory.
     * @param fileName The file name of the shell file.
     * @param content  The content of the shell file.
     */
    public void createShellFile(
        @NotNull final String fileName,
        @NotNull final String content
    ) {
        try {
            final var filePath = Burrow.BIN_ROOT.resolve(fileName);
            Files.write(filePath, content.getBytes());

            // Change the permission of the bin file to 744
            final var permissions = PosixFilePermissions.fromString("rwxr--r--");
            Files.setPosixFilePermissions(filePath, permissions);
        } catch (final IOException ex) {
            logger.error("Fail to create a shell file for chamber <{}>", chamber.getName(), ex);
        }
    }

    public void createShellFile(@NotNull final String content) {
        final var shellFileName = getConfig().getNonNull(ConfigKey.BIN_NAME);
        createShellFile(shellFileName, content);
    }

    public boolean createShellFileIfAbsent(@NotNull final String content) {
        final var shellFileName = getConfig().getNonNull(ConfigKey.BIN_NAME);
        final var file = new File(shellFileName);
        if (!file.exists()) {
            createShellFile(content);
            return true;
        }

        return false;
    }

    public @NotNull String getDefaultShellContent() {
        final var shell = getConfig().getNonNull(ConfigKey.BIN_SHELL);
        final var chamberName = chamber.getName();
        return "#! " + shell + "\n\nburrow " + chamberName + " \"$@\"";
    }

    public @interface ConfigKey {
        String BIN_SHELL = "bin.shell";
        String BIN_NAME = "bin.name";
    }
}
