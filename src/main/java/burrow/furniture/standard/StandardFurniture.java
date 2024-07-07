package burrow.furniture.standard;

import burrow.core.chamber.Chamber;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.DefaultCommand;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.core.furniture.FurnitureNotFoundException;
import burrow.core.furniture.Renovator;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

@BurrowFurniture(
    simpleName = "Standard",
    description = "Standard commands."
)
public class StandardFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Standard";

    public StandardFurniture(@NonNull final Chamber chamber) {
        super(chamber);
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
    public static String stringListToString(
        @NonNull final CommandContext commandContext,
        @NonNull final List<String> stringList,
        final boolean useMultiLine
    ) {
        if (useMultiLine) {
            final var lines = String.join("\n", stringList).split("\n");
            final var indentedLineList = Arrays.stream(lines).map(line -> "  " + line).toList();
            return "[\n" + String.join("\n", indentedLineList) + "\n]";
        } else {
            final var string = "[" + String.join(", ", stringList) + "]";
            final var environment = commandContext.getEnvironment();
            final var consoleWidth = Objects.requireNonNull(environment.getConsoleWidth());
            if (string.length() < consoleWidth) {
                return string;
            } else {
                return stringListToString(commandContext, stringList, true);
            }
        }
    }

    @NonNull
    public static String stringListToString(
        @NonNull final CommandContext commandContext,
        @NonNull final List<String> stringList
    ) {
        return stringListToString(commandContext, stringList, false);
    }

    @Override
    public void beforeInitialization() {
        registerCommand(RootCommand.class);
        registerCommand(HelpCommand.class);
        registerCommand(ConfigListCommand.class);
        registerCommand(ConfigCommand.class);
        registerCommand(ConfigListCommand.class);
        registerCommand(FurnitureListCommand.class);
        registerCommand(FurnitureAddCommand.class);
        registerCommand(FurnitureAllCommand.class);
        registerCommand(FurnitureRemoveCommand.class);
        registerCommand(DescriptionCommand.class);
        registerCommand(CommandListCommand.class);
    }

    @NonNull
    public Path getRootAbsolutePath() {
        return getChamberContext().getRootPath().toAbsolutePath().normalize();
    }

    public void updateConfigItem(@NonNull final String key, @NonNull final String value) {
        getConfig().set(key, value);
    }

    @Nullable
    public String retrieveConfigItem(@NonNull final String key) {
        return getConfig().get(key);
    }

    @NonNull
    public Collection<String> getConfigKeys(@NonNull final String furnitureName) throws
        FurnitureNotFoundException {
        final var configKeys = use(furnitureName).configKeys();
        return Optional.ofNullable(configKeys).orElse(new ArrayList<>());
    }

    @NonNull
    public List<String> getConfigFurnitureNameList() {
        final var furnitureListString =
            getConfig().getNonNull(Config.Key.CHAMBER_FURNITURE_LIST);
        return Arrays.stream(furnitureListString.split(Renovator.FURNITURE_NAME_SEPARATOR))
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .toList();
    }

    public List<? extends Class<? extends Furniture>> getFurnitureClassList() {
        return getRenovator().getFurnitureStore().values().stream()
            .map(Furniture::getClass).toList();
    }

    @NonNull
    public Collection<Class<? extends Command>> getCommandClassList(
        @NonNull final List<String> furnitureNameList
    ) throws FurnitureNotFoundException {
        final var list = new ArrayList<Class<? extends Command>>();
        for (final var furnitureName : furnitureNameList) {
            list.addAll(use(furnitureName).getAllCommands());
        }
        list.remove(DefaultCommand.class);

        return list;
    }

    @NonNull
    public Collection<Class<? extends Command>> getCommandClassList() {
        final var list = new ArrayList<>(getProcessor().getCommandClassStore().values());
        list.remove(DefaultCommand.class);

        return list;
    }

    @NonNull
    public String getChamberDescription() {
        return getConfig().getNonNull(Config.Key.CHAMBER_DESCRIPTION);
    }

    public void updateChamberDescription(
        @NonNull final String description
    ) {
        getConfig().set(Config.Key.CHAMBER_DESCRIPTION, description);
    }
}
