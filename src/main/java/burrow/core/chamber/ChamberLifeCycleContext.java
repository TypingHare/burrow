package burrow.core.chamber;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import org.springframework.lang.NonNull;

import java.time.Instant;

public final class ChamberLifeCycleContext extends Context {
    public @interface Hook {
        ContextHook<Chamber> chamber = hook("chamber");
        ContextHook<Instant> creationTime = hook("creationTime");
    }

    @NonNull
    public Chamber getChamber() {
        return Hook.chamber.getNonNull(this);
    }

    public void setChamber(@NonNull final Chamber chamber) {
        Hook.chamber.set(this, chamber);
    }

    @NonNull
    public Instant getCreationTime() {
        return Hook.creationTime.getNonNull(this);
    }

    public void setCreationTime(@NonNull final Instant creationTime) {
        Hook.creationTime.set(this, creationTime);
    }
}
