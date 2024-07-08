package burrow.furniture.bincore;

import burrow.core.Burrow;
import burrow.core.chamber.Chamber;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.List;

@BurrowFurniture(
    simpleName = "Bin Core",
    description = "Bin Core allows developers to create shell files."
)
public class BinCoreFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Bin Core";
    public static final String DEFAULT_BIN_SHELL = "/bin/zsh";
    public static final String BIN_NAME_PREFIX = "b";

    public BinCoreFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public Collection<String> configKeys() {
        return List.of(ConfigKey.BIN_SHELL, ConfigKey.BIN_NAME);
    }

    @Override
    public void initializeConfig(@NonNull final Config config) {
        config.setIfAbsent(ConfigKey.BIN_SHELL, DEFAULT_BIN_SHELL);
        config.setIfAbsent(ConfigKey.BIN_NAME, BIN_NAME_PREFIX + chamber.getName());
    }

    @Override
    public void beforeInitialization() {
        registerCommand(CreateBinCommand.class);
    }

    public void createBinFile(
        @NonNull final String fileName,
        @NonNull final String content
    ) throws IOException {
        final var filePath = Burrow.BIN_ROOT.resolve(fileName);
        Files.write(filePath, content.getBytes());

        // Change the permission of the bin file to 744x
        final var permissions = PosixFilePermissions.fromString("rwxr--r--");
        Files.setPosixFilePermissions(filePath, permissions);
    }

    public void createBinFile(@NonNull final String content) throws IOException {
        final var binFileName = getConfig().getNonNull(ConfigKey.BIN_NAME);
        createBinFile(binFileName, content);
    }

    public void createBinFile() throws IOException {
        final var shell = getConfig().getNonNull(ConfigKey.BIN_SHELL);
        final var chamberName = chamber.getName();
        final var content = "#! " + shell + "\n\nburrow " + chamberName + " \"$@\"";
        createBinFile(content);
    }

    public @interface ConfigKey {
        String BIN_SHELL = "bin.shell";
        String BIN_NAME = "bin.name";
    }
}
