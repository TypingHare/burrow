package burrow.core.chamber;

import burrow.core.Burrow;
import burrow.core.command.ExecuteCommandChain;
import burrow.core.command.Processor;
import burrow.core.config.Config;
import burrow.core.furniture.Renovator;
import org.springframework.lang.NonNull;

public final class Chamber {
    private final ChamberShepherd chamberShepherd;
    private final String name;
    private final ChamberContext chamberContext;
    private final ExecuteCommandChain executeCommandChain;

    public Chamber(
        @NonNull final ChamberShepherd chamberShepherd,
        @NonNull final String name
    ) {
        this.chamberShepherd = chamberShepherd;
        this.name = name;
        this.chamberContext = new ChamberContext();
        this.executeCommandChain = new ExecuteCommandChain();
    }

    @NonNull
    public ChamberShepherd getChamberShepherd() {
        return chamberShepherd;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public ChamberContext getChamberContext() {
        return chamberContext;
    }

    @NonNull
    public ExecuteCommandChain getExecuteCommandChain() {
        return executeCommandChain;
    }

    public void initialize() throws ChamberInitializationException {
        try {
            ChamberContext.Hook.chamber.set(chamberContext, this);

            final var rootPath = Burrow.CHAMBERS_ROOT_DIR.resolve(name);
            final var config = new Config(this);
            final var renovator = new Renovator(this);
            chamberContext.setRootPath(rootPath);
            chamberContext.setConfig(config);
            chamberContext.setRenovator(renovator);
            chamberContext.setProcessor(new Processor(this));

            // Check chamber directory
            if (!rootPath.toFile().isDirectory()) {
                throw new ChamberNotFoundException(name);
            }

            config.loadFromFile();
            renovator.loadFurniture();
        } catch (final Throwable ex) {
            throw new ChamberInitializationException(ex);
        }
    }

    public void terminate() {
        ChamberContext.Hook.config.getNonNull(chamberContext).saveToFile();

        final var renovator = ChamberContext.Hook.renovator.getNonNull(chamberContext);
        renovator.terminateAllFurniture();
    }

    public void restart() throws ChamberInitializationException {
        terminate();

        final var chamberShepherd = getChamberShepherd();
        chamberShepherd.initialize(name);
    }
}
