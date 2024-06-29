package burrow.furniture.standard;

import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberContext;
import burrow.core.command.Command;
import burrow.core.config.Config;
import burrow.core.furniture.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import picocli.CommandLine;

import java.util.*;
import java.util.function.Predicate;

@BurrowFurniture(
    simpleName = "standard",
    description = "Standard commands."
)
public class StandardFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Standard";

    public StandardFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public void init() {
        registerCommand(RootCommand.class);
        registerCommand(HelpCommand.class);
        registerCommand(CommandList.class);
        registerCommand(ConfigCommand.class);
        registerCommand(ConfigListCommand.class);
        registerCommand(FurnitureListCommand.class);
        registerCommand(FurnitureAddCommand.class);
        registerCommand(FurnitureRemoveCommand.class);
    }

    @NonNull
    public static String getRootDirectoryAbsolutePath(
        @NonNull final ChamberContext chamberContext
    ) {
        return chamberContext.getRootDir().toString();
    }

    public static void updateConfigItem(
        @NonNull final ChamberContext chamberContext,
        @NonNull final String key,
        @NonNull final String value
    ) {
        chamberContext.getConfig().set(key, value);
    }

    @Nullable
    public static String retrieveConfigItem(
        @NonNull final ChamberContext chamberContext,
        @NonNull final String key
    ) {
        return chamberContext.getConfig().get(key);
    }

    @NonNull
    public static Collection<String> getConfigKeys(
        @NonNull final ChamberContext chamberContext,
        @NonNull final String furnitureName
    ) throws FurnitureNotFoundException, AmbiguousSimpleNameException {
        return Optional.ofNullable(chamberContext.getRenovator().getFurnitureByName(furnitureName)
            .configKeys()).orElse(new ArrayList<>());
    }

    @NonNull
    public static List<String> getConfigFurnitureNameList(
        @NonNull final ChamberContext chamberContext
    ) {
        final var furnitureListString = chamberContext.getConfig().get(Config.Key.FURNITURE_LIST);
        assert furnitureListString != null;
        return Arrays.stream(furnitureListString.split(Renovator.FURNITURE_NAME_SEPARATOR))
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .toList();
    }

    @NonNull
    public static List<String> getFurnitureFullNameList(
        @NonNull final ChamberContext chamberContext) {
        return chamberContext.getRenovator().getAllFullNames();
    }

    @NonNull
    public static List<String> getFurnitureSimpleNameList(
        @NonNull final ChamberContext chamberContext) {
        return chamberContext.getRenovator().getFullNameMap().keySet().stream().toList();
    }

    @NonNull
    public static Map<String, List<String>> getFurnitureFullNameMap(
        @NonNull final ChamberContext chamberContext
    ) {
        return chamberContext.getRenovator().getFullNameMap();
    }

    @NonNull
    public static Collection<Class<? extends Command>> getCommandClassList(
        @NonNull final ChamberContext chamberContext,
        @Nullable final String furnitureName
    )
        throws FurnitureNotFoundException, AmbiguousSimpleNameException {
        return furnitureName == null
            ? chamberContext.getProcessor().getAllCommands()
            : chamberContext.getRenovator().getFurnitureByName(furnitureName).getAllCommands();
    }

    @NonNull
    public static Map<String, Collection<Class<? extends Command>>> classifyCommandClasses(
        @NonNull final Collection<Class<? extends Command>> commandClassList
    ) {
        final var map = new HashMap<String, Collection<Class<? extends Command>>>();
        for (final var commandClass : commandClassList) {
            final var commandType = Command.getType(commandClass);
            map.computeIfAbsent(commandType, k -> new ArrayList<>()).add(commandClass);
        }

        return map;
    }

    @NonNull
    public static Collection<Class<? extends Command>> sortCommandClassList(
        @NonNull Collection<Class<? extends Command>> commandClassList
    ) {
        return commandClassList.stream().sorted(Comparator.comparing(Command::getName)).toList();
    }

    @NonNull
    public static String getChamberDescription(@NonNull final ChamberContext chamberContext) {
        return chamberContext.getConfig().getRequireNotNull(Config.Key.CHAMBER_DESCRIPTION);
    }

    public static void updateChamberDescription(
        @NonNull final ChamberContext chamberContext,
        @NonNull final String description
    ) {
        chamberContext.getConfig().set(Config.Key.CHAMBER_DESCRIPTION, description);
    }
}
