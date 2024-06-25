package burrow.furniture.standard;

import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberContext;
import burrow.core.config.Config;
import burrow.core.furniture.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Predicate;

@BurrowFurniture(
    simpleName = "standard",
    description = "Standard commands."
)
public class StandardFurniture extends Furniture {
    public static final String COMMAND_TYPE = "standard";

    public StandardFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public void init() {
        registerCommand(RootCommand.class);
        registerCommand(ConfigCommand.class);
        registerCommand(ConfigListCommand.class);
        registerCommand(FurnitureListCommand.class);
        registerCommand(FurnitureAddCommand.class);
        registerCommand(FurnitureRemoveCommand.class);
    }

    @NonNull
    public static String getRootDirectoryAbsolutePath(
        @NonNull final ChamberContext context
    ) {
        return context.getRootDir().toString();
    }

    public static void updateConfigItem(
        @NonNull final ChamberContext context,
        @NonNull final String key,
        @NonNull final String value
    ) {
        context.getConfig().set(key, value);
    }

    @Nullable
    public static String retrieveConfigItem(
        @NonNull final ChamberContext context,
        @NonNull final String key
    ) {
        return context.getConfig().get(key);
    }

    @NonNull
    public static Collection<String> getConfigKeys(
        @NonNull final ChamberContext context,
        @NonNull final String furnitureName
    ) throws FurnitureNotFoundException, AmbiguousSimpleNameException {
        return Optional.ofNullable(context.getRenovator().getFurnitureByName(furnitureName)
            .configKeys()).orElse(new ArrayList<>());
    }

    @NonNull
    public static List<String> getConfigFurnitureNameList(
        @NonNull final ChamberContext context
    ) {
        final var furnitureListString = context.getConfig().get(Config.Key.FURNITURE_LIST);
        assert furnitureListString != null;
        return Arrays.stream(furnitureListString.split(Renovator.FURNITURE_NAME_SEPARATOR))
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .toList();
    }

    @NonNull
    public static List<String> getFurnitureFullNameList(@NonNull final ChamberContext context) {
        return context.getRenovator().getAllFullNames();
    }

    @NonNull
    public static List<String> getFurnitureSimpleNameList(@NonNull final ChamberContext context) {
        return context.getRenovator().getFullNameMap().keySet().stream().toList();
    }

    @NonNull
    public static Map<String, List<String>> getFurnitureFullNameMap(
        @NonNull final ChamberContext context
    ) {
        return context.getRenovator().getFullNameMap();
    }
}
