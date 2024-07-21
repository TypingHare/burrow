package burrow.furniture.standard;

import burrow.core.chamber.Chamber;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.DefaultCommand;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.core.furniture.Renovator;
import burrow.core.furniture.exception.FurnitureNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

@BurrowFurniture(
    simpleName = "Standard",
    description = "A collection of standard commands.",
    type = BurrowFurniture.Type.COMPONENT
)
public class StandardFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Standard";

    public StandardFurniture(@NotNull final Chamber chamber) {
        super(chamber);
    }

    @NotNull
    public static Map<String, Collection<Class<? extends Command>>> classifyCommandClasses(
        @NotNull final Collection<Class<? extends Command>> commandClassList
    ) {
        final var map = new HashMap<String, Collection<Class<? extends Command>>>();
        for (final var commandClass : commandClassList) {
            final var commandType = Command.getType(commandClass);
            map.computeIfAbsent(commandType, k -> new ArrayList<>()).add(commandClass);
        }

        return map;
    }

    @NotNull
    public static Collection<Class<? extends Command>> sortCommandClassList(
        @NotNull Collection<Class<? extends Command>> commandClassList
    ) {
        return commandClassList.stream().sorted(Comparator.comparing(Command::getName)).toList();
    }

    @NotNull
    public static String stringListToString(
        @NotNull final CommandContext commandContext,
        @NotNull final List<String> stringList,
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

    @NotNull
    public static String stringListToString(
        @NotNull final CommandContext commandContext,
        @NotNull final List<String> stringList
    ) {
        return stringListToString(commandContext, stringList, false);
    }

    @Override
    public void beforeInitialization() {
        registerCommand(RootCommand.class);
        registerCommand(HelpCommand.class);
        registerCommand(ConfigListCommand.class);
        registerCommand(ConfigValueCommand.class);
        registerCommand(ConfigSetCommand.class);
        registerCommand(FurnitureListCommand.class);
        registerCommand(FurnitureAllCommand.class);
        registerCommand(FurnitureAddCommand.class);
        registerCommand(FurnitureRemoveCommand.class);
        registerCommand(DescriptionCommand.class);
        registerCommand(CommandListCommand.class);
    }

    @NotNull
    public Path getRootAbsolutePath() {
        return getChamberContext().getRootPath().toAbsolutePath().normalize();
    }

    public void updateConfigItem(@NotNull final String key, @NotNull final String value) {
        getConfig().set(key, value);
    }

    @Nullable
    public String retrieveConfigItem(@NotNull final String key) {
        return getConfig().get(key);
    }

    @NotNull
    public Collection<String> getConfigKeys(@NotNull final String furnitureName) throws
        FurnitureNotFoundException {
        final var configKeys = use(furnitureName).configKeys();
        return Optional.ofNullable(configKeys).orElse(new ArrayList<>());
    }

    @NotNull
    public List<String> getConfigFurnitureNameList() {
        final var furnitureListString =
            getConfig().getNotNull(Config.Key.CHAMBER_FURNITURE_LIST);
        return Arrays.stream(furnitureListString.split(Renovator.FURNITURE_NAME_SEPARATOR))
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .toList();
    }

    public List<? extends Class<? extends Furniture>> getFurnitureClassList() {
        return getRenovator().getFurnitureStore().values().stream()
            .map(Furniture::getClass).toList();
    }

    @NotNull
    public Collection<Class<? extends Command>> getCommandClassList(
        @NotNull final List<String> furnitureNameList
    ) throws FurnitureNotFoundException {
        final var list = new ArrayList<Class<? extends Command>>();
        for (final var furnitureName : furnitureNameList) {
            list.addAll(use(furnitureName).getCommandList());
        }
        list.remove(DefaultCommand.class);

        return list;
    }

    @NotNull
    public Collection<Class<? extends Command>> getCommandClassList() {
        final var list = new ArrayList<>(getProcessor().getCommandClassStore().values());
        list.remove(DefaultCommand.class);

        return list;
    }

    @NotNull
    public String getChamberDescription() {
        return getConfig().getNotNull(Config.Key.CHAMBER_DESCRIPTION);
    }

    public void updateChamberDescription(
        @NotNull final String description
    ) {
        getConfig().set(Config.Key.CHAMBER_DESCRIPTION, description);
    }
}
