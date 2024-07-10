package burrow.furniture.aspectcore;

import burrow.chain.Middleware;
import burrow.core.Burrow;
import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberLifeCycleContext;
import burrow.core.chamber.ChamberShepherd;
import burrow.core.command.CommandContext;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.core.furniture.FurnitureRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BurrowFurniture(
    simpleName = "Aspect Core",
    description = "Aspect Core allows developers to manage scopes out of chamber easily."
)
public class AspectCoreFurniture extends Furniture {
    public AspectCoreFurniture(@NotNull final Chamber chamber) {
        super(chamber);
    }

    @NotNull
    public ChamberShepherd getChamberShepherd() {
        return chamber.getChamberShepherd();
    }

    @NotNull
    public Burrow getBurrow() {
        return getChamberShepherd().getBurrow();
    }

    @NotNull
    public FurnitureRegistrar getFurnitureRegistrar() {
        return getBurrow().getFurnitureRegistrar();
    }

    @NotNull
    public List<String> getRunningChamberNameList() {
        return getChamberShepherd().getChamberStore().keySet().stream().toList();
    }

    public void beforeCreateChamber(
        @NotNull final Middleware.Pre<ChamberLifeCycleContext> middleware
    ) {
        getChamberShepherd().getCreateChamberChain().use(middleware);
    }

    public void afterCreateChamber(
        @NotNull final Middleware.Post<ChamberLifeCycleContext> middleware
    ) {
        getChamberShepherd().getCreateChamberChain().use(middleware);
    }

    public void beforeTerminateChamber(
        @NotNull final Middleware.Pre<ChamberLifeCycleContext> middleware
    ) {
        getChamberShepherd().getTerminateChamberChain().use(middleware);
    }

    public void afterTerminateChamber(
        @NotNull final Middleware.Post<ChamberLifeCycleContext> middleware
    ) {
        getChamberShepherd().getTerminateChamberChain().use(middleware);
    }

    public void beforeProcessCommand(
        @NotNull final Middleware.Pre<CommandContext> middleware
    ) {
        getChamberShepherd().getProcessCommandChain().use(middleware);
    }

    public void afterProcessCommand(
        @NotNull final Middleware.Post<CommandContext> middleware
    ) {
        getChamberShepherd().getProcessCommandChain().use(middleware);
    }
}
