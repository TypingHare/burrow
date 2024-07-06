package burrow.core.chamber;

import burrow.core.command.Processor;
import burrow.core.config.Config;
import burrow.core.furniture.Renovator;
import org.springframework.lang.NonNull;

public abstract class ChamberModule {
    protected final Chamber chamber;

    public ChamberModule(@NonNull final Chamber chamber) {
        this.chamber = chamber;
    }

    @NonNull
    public Chamber getChamber() {
        return chamber;
    }

    @NonNull
    public ChamberContext getChamberContext() {
        return chamber.getChamberContext();
    }

    @NonNull
    public Config getConfig() {
        return getChamberContext().getConfig();
    }

    @NonNull
    public Renovator getRenovator() {
        return getChamberContext().getRenovator();
    }

    @NonNull
    public Processor getProcessor() {
        return getChamberContext().getProcessor();
    }
}
