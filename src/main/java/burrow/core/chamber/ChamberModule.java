package burrow.core.chamber;

import burrow.core.command.Processor;
import burrow.core.config.Config;
import burrow.core.furniture.Renovator;
import org.jetbrains.annotations.NotNull;

public abstract class ChamberModule {
    protected final Chamber chamber;

    public ChamberModule(@NotNull final Chamber chamber) {
        this.chamber = chamber;
    }

    @NotNull
    public Chamber getChamber() {
        return chamber;
    }

    @NotNull
    public ChamberContext getChamberContext() {
        return chamber.getChamberContext();
    }

    @NotNull
    public Config getConfig() {
        return getChamberContext().getConfig();
    }

    @NotNull
    public Renovator getRenovator() {
        return getChamberContext().getRenovator();
    }

    @NotNull
    public Processor getProcessor() {
        return getChamberContext().getProcessor();
    }
}
