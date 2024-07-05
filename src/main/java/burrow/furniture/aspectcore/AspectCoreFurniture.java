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
import org.springframework.lang.NonNull;

import java.util.List;

@BurrowFurniture(
    simpleName = "Aspect Core",
    description = "Aspect Core allows developers to manage scopes out of chamber easily."
)
public class AspectCoreFurniture extends Furniture {
    public AspectCoreFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @NonNull
    public ChamberShepherd getChamberShepherd() {
        return chamber.getChamberShepherd();
    }

    @NonNull
    public Burrow getBurrow() {
        return getChamberShepherd().getBurrow();
    }

    @NonNull
    public FurnitureRegistrar getFurnitureRegistrar() {
        return getBurrow().getFurnitureRegistrar();
    }

    @NonNull
    public List<String> getRunningChamberNameList() {
        return getChamberShepherd().getChamberStore().keySet().stream().toList();
    }

    public void beforeCreateChamber(
        @NonNull final Middleware.Pre<ChamberLifeCycleContext> middleware
    ) {
        getChamberShepherd().getCreateChamberChain().use(middleware);
    }

    public void afterCreateChamber(
        @NonNull final Middleware.Post<ChamberLifeCycleContext> middleware
    ) {
        getChamberShepherd().getCreateChamberChain().use(middleware);
    }

    public void beforeTerminateChamber(
        @NonNull final Middleware.Pre<ChamberLifeCycleContext> middleware
    ) {
        getChamberShepherd().getTerminateChamberChain().use(middleware);
    }

    public void afterTerminateChamber(
        @NonNull final Middleware.Post<ChamberLifeCycleContext> middleware
    ) {
        getChamberShepherd().getTerminateChamberChain().use(middleware);
    }

    public void beforeProcessCommand(
        @NonNull final Middleware.Pre<CommandContext> middleware
    ) {
        getChamberShepherd().getProcessCommandChain().use(middleware);
    }

    public void afterProcessCommand(
        @NonNull final Middleware.Post<CommandContext> middleware
    ) {
        getChamberShepherd().getProcessCommandChain().use(middleware);
    }
}
