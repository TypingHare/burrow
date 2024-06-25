package burrow.core.furniture;

import burrow.core.chain.CreateEntryChain;
import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberModule;
import burrow.core.command.Command;
import burrow.core.config.Config;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Furniture extends ChamberModule {
    private final Set<Class<? extends Command>> commandSet = new LinkedHashSet<>();

    public Furniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    /**
     * Initializes this furniture. This method will be called after all of its dependencies are
     * resolved.
     */
    public void init() {
    }

    /**
     * Terminates this furniture. This method will be called when the chamber is terminated.
     */
    public void terminate() {

    }

    @Nullable
    public Collection<String> configKeys() {
        return null;
    }

    public void initConfig(@NonNull final Config config) {
    }

    /**
     * Uses a specified furniture. The used furniture must be a dependency of this furniture.
     * @param furnitureClass The class of the furniture to use.
     * @param <T>            The type of the furniture.
     * @return The furniture object
     */
    @NonNull
    public <T extends Furniture> T use(final Class<T> furnitureClass) {
        return context.getRenovator().getFurniture(furnitureClass);
    }

    public void registerCommand(@NonNull final Class<? extends Command> commandClass) {
        commandSet.add(commandClass);
        context.getProcessor().register(commandClass);
    }

    @NonNull
    public Collection<Class<? extends Command>> getAllCommands() {
        return commandSet;
    }

    @NonNull
    public CreateEntryChain getCreateEntryChain() {
        return context.getOverseer().getCreateEntryChain();
    }
}
