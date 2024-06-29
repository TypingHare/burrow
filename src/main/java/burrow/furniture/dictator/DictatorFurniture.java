package burrow.furniture.dictator;

import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberContext;
import burrow.core.chamber.ChamberShepherd;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.standard.StandardFurniture;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@BurrowFurniture(
    simpleName = "dictator",
    description = "Allows users to create, delete, and monitor chambers."
)
public class DictatorFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Dictator";

    public DictatorFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public void init() {
        registerCommand(ChamberNewCommand.class);
        registerCommand(ChamberListCommand.class);
    }

    @Override
    public Collection<String> configKeys() {
        return List.of(ConfigKey.DICTATOR_DEFAULT_FURNITURE_LIST);
    }

    @Override
    public void initConfig(@NonNull final Config config) {
        config.setIfAbsent(ConfigKey.DICTATOR_DEFAULT_FURNITURE_LIST, StandardFurniture.class.getName());
    }

    @NonNull
    public static List<String> getAllChambers() throws IOException {
        final List<String> chamberList = new ArrayList<>();
        final var chamberRootDirString = ChamberShepherd.CHAMBER_ROOT_DIR.toString();
        final var prefixLength = 1 + chamberRootDirString.length();
        try (final var stream = getChamberDirectoryStream()) {
            for (final var path : stream) {
                chamberList.add(path.toString().substring(prefixLength));
            }
        }

        return chamberList;
    }

    @NonNull
    public static DirectoryStream<Path> getChamberDirectoryStream() throws IOException {
        return Files.newDirectoryStream(ChamberShepherd.CHAMBER_ROOT_DIR, Files::isDirectory);
    }

    public static Config createNewConfig(
        @NonNull final ChamberContext context,
        @NonNull final Path rootDirectory
    ) {
        final var applicationContext = context.getChamber().getApplicationContext();
        final var chamber = applicationContext.getBean(Chamber.class);
        final var configFile = rootDirectory.resolve(Config.CONFIG_FILE_NAME).normalize().toFile();
        chamber.getContext().set(ChamberContext.Key.CONFIG_FILE, configFile);

        return applicationContext.getBean(Config.class, chamber);
    }

    public final static class ConfigKey {
        public static final String DICTATOR_DEFAULT_FURNITURE_LIST =
            "dictator.default_furniture_list";
    }
}
