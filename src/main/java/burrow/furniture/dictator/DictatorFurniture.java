package burrow.furniture.dictator;

import burrow.core.Burrow;
import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberContext;
import burrow.core.chamber.ChamberInitializationException;
import burrow.core.command.CommandContext;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.core.furniture.Renovator;
import burrow.furniture.aspectcore.AspectCoreFurniture;
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
        StandardFurniture.class.getName()
    );
    public static final String COMMAND_TYPE = "Dictator";
    private final Map<String, ChamberInfo> chamberInfoMap = new HashMap<>();

    public DictatorFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public void beforeInitialization() {
        registerCommand(ChamberNewCommand.class);
        registerCommand(ChamberListCommand.class);
        registerCommand(StartCommand.class);
        registerCommand(TerminateCommand.class);
        registerCommand(ChamberInfoCommand.class);
    }

    @NonNull
    public Map<String, ChamberInfo> getChamberInfoMap() {
        return chamberInfoMap;
    }

    @Override
    public void initialize() {
        final var aspectFurniture = use(AspectCoreFurniture.class);
        aspectFurniture.beforeProcessCommand((context) -> {
            final var chamberContext = CommandContext.Hook.chamberContext.getNonNull(context);
            final var chamber = ChamberContext.Hook.chamber.getNonNull(chamberContext);
            final var chamberName = chamber.getName();
            if (!chamberInfoMap.containsKey(chamberName)) {
                final var chamberInfo = new ChamberInfo();
                chamberInfo.setStartTimestampMs(System.currentTimeMillis());
                chamberInfoMap.put(chamberName, chamberInfo);
            }

            final var chamberInfo = chamberInfoMap.get(chamberName);
            chamberInfo.setLastRequestTimestampMs(System.currentTimeMillis());
        });

        super.initialize();
    }

    @Override
    public Collection<String> configKeys() {
        return List.of(ConfigKey.DICTATOR_DEFAULT_FURNITURE_LIST);
    }

    @Override
    public void initializeConfig(@NonNull final Config config) {
        config.setIfAbsent(
            ConfigKey.DICTATOR_DEFAULT_FURNITURE_LIST,
            String.join(Renovator.FURNITURE_NAME_SEPARATOR, DEFAULT_FURNITURE_LIST)
        );
    }

    @NonNull
    public static List<String> getAllChambers() throws IOException {
        final List<String> chamberList = new ArrayList<>();
        final var chamberRootDirString = Burrow.CHAMBERS_ROOT_DIR.toString();
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
        return Files.newDirectoryStream(Burrow.CHAMBERS_ROOT_DIR, Files::isDirectory);
    }

    public void start(@NonNull final String chamberName) throws ChamberInitializationException {
        final var aspectCoreFurniture = use(AspectCoreFurniture.class);
        aspectCoreFurniture.getChamberShepherd().getOrStartChamber(chamberName);
    }

    public void terminate(@NonNull final String chamberName) {
        final var aspectCoreFurniture = use(AspectCoreFurniture.class);
        aspectCoreFurniture.getChamberShepherd().terminate(chamberName);
    }

    public @interface ConfigKey {
        String DICTATOR_DEFAULT_FURNITURE_LIST = "dictator.default_furniture_list";
    }
}
