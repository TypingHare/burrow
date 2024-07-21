package burrow.core.chamber;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public final class ChamberLifeCycleContext extends Context {
    @NotNull
    public Chamber getChamber() {
        return Hook.chamber.getNotNull(this);
    }

    public void setChamber(@NotNull final Chamber chamber) {
        Hook.chamber.set(this, chamber);
    }

    @NotNull
    public Instant getCreationTime() {
        return Hook.creationTime.getNotNull(this);
    }

    public void setCreationTime(@NotNull final Instant creationTime) {
        Hook.creationTime.set(this, creationTime);
    }

    public @interface Hook {
        ContextHook<Chamber> chamber = hook("chamber");
        ContextHook<Instant> creationTime = hook("creationTime");
    }
}
