package burrow.core.furniture;

import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberModule;
import burrow.core.command.Command;
import burrow.core.config.Config;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;

public abstract class Furniture extends ChamberModule {
    private final Set<Class<? extends Command>> commandSet = new LinkedHashSet<>();
    private boolean isInitialized = false;

    public Furniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @NonNull
    public static String getSimpleName(@NonNull final Class<? extends Furniture> furnitureClass) {
        final var annotation = furnitureClass.getAnnotation(BurrowFurniture.class);
        assert annotation != null;

        return annotation.simpleName();
    }

    public void setInitialized(final boolean initialized) {
        isInitialized = initialized;
    }

    /**
     * Register commands here. "Use" is not able to be used here.
     */
    @SuppressWarnings("EmptyMethod")
    public void beforeInitialization() {
    }

    /**
     * Initializes this furniture. This method will be called after all of its dependencies are
     * resolved.
     */
    @SuppressWarnings("EmptyMethod")
    public void initialize() {
    }

    /**
     * This method is called after all its dependencies are initialized.
     */
    @SuppressWarnings("EmptyMethod")
    public void afterInitialization() {
    }

    /**
     * Terminates this furniture. This method will be called when the chamber is terminated.
     */
    @SuppressWarnings("EmptyMethod")
    public void terminate() {
    }

    @Nullable
    public Collection<String> configKeys() {
        return null;
    }

    @SuppressWarnings({"EmptyMethod", "unused"})
    public void initializeConfig(@NonNull final Config config) {
    }

    @NonNull
    public <T extends Furniture> T use(@NonNull final String furnitureName) {
        if (!isInitialized) {
            throw new RuntimeException("The use() method is not allowed to be called before the initialization.");
        }

        final T furniture = getRenovator().getFurniture(furnitureName);
        if (furniture == null) {
            throw new FurnitureNotFoundException(furnitureName);
        }

        return furniture;
    }

    @NonNull
    public <T extends Furniture> T use(@NonNull final Class<T> furnitureClass) {
        return use(furnitureClass.getName());
    }

    public void registerCommand(@NonNull final Class<? extends Command> commandClass) {
        commandSet.add(commandClass);
        getProcessor().register(commandClass);
    }

    @NonNull
    public List<Class<? extends Command>> getAllCommands() {
        return new ArrayList<>(commandSet);
    }
}
