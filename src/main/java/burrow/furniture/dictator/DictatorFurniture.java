package burrow.furniture.dictator;

import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberContext;
import burrow.core.chamber.ChamberShepherd;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.core.furniture.Renovator;
import burrow.furniture.aspectcore.AspectCoreFurniture;
import burrow.furniture.entry.EntryFurniture;
import burrow.furniture.standard.StandardFurniture;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@BurrowFurniture(
    simpleName = "Dictator",
    description = "Allows users to create, delete, and monitor chambers.",
    dependencies = {
        AspectCoreFurniture.class
    }
)
public class DictatorFurniture extends Furniture {
    private static final List<String> DEFAULT_FURNITURE_LIST = List.of(
        StandardFurniture.class.getName(),
        EntryFurniture.class.getName()
    );
    public static final String COMMAND_TYPE = "Dictator";
    private final Map<String, ChamberInfo> chamberInfoMap = new HashMap<>();

    public DictatorFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @NonNull
    public Map<String, ChamberInfo> getChamberInfoMap() {
        return chamberInfoMap;
    }

    @Override
    public void init() {
        registerCommand(ChamberNewCommand.class);
        registerCommand(ChamberListCommand.class);
        registerCommand(TerminateCommand.class);
        registerCommand(ChamberInfoCommand.class);

        final var aspectFurniture = use(AspectCoreFurniture.class);

        aspectFurniture.onBeforeExecution((ctx) -> {
            final var chamberName = ctx.getChamber().getContext().getChamberName();
            if (!chamberInfoMap.containsKey(chamberName)) {
                final var chamberInfo = new ChamberInfo();
                chamberInfo.setInitiateTimestampMs(System.currentTimeMillis());
                chamberInfoMap.put(chamberName, chamberInfo);
            }

            final var chamberInfo = chamberInfoMap.get(chamberName);
            chamberInfo.setLastRequestTimestampMs(System.currentTimeMillis());
        });
    }

    @Override
    public Collection<String> configKeys() {
        return List.of(ConfigKey.DICTATOR_DEFAULT_FURNITURE_LIST);
    }

    @Override
    public void initConfig(@NonNull final Config config) {
        config.setIfAbsent(
            ConfigKey.DICTATOR_DEFAULT_FURNITURE_LIST,
            String.join(Renovator.FURNITURE_NAME_SEPARATOR, DEFAULT_FURNITURE_LIST)
        );
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

    public static Set<String> getAllRunningChambers(
        @NonNull final ChamberContext chamberContext
    ) {
        final var aspectCoreFurniture =
            chamberContext.getRenovator().getFurniture(AspectCoreFurniture.class);
        final var chamberShepherd = aspectCoreFurniture.getChamberShepherd();
        return aspectCoreFurniture.getChamberShepherd().getChamberStore().keySet();
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

    public static void terminate(
        @NonNull final ChamberContext chamberContext,
        @NonNull final String chamberName
    ) {
        final var aspectCoreFurniture =
            chamberContext.getRenovator().getFurniture(AspectCoreFurniture.class);
        aspectCoreFurniture.getChamberShepherd().terminate(chamberName);
    }

    public final static class ConfigKey {
        public static final String DICTATOR_DEFAULT_FURNITURE_LIST =
            "dictator.default_furniture_list";
    }
}
