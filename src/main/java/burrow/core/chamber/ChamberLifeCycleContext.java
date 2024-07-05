package burrow.core.chamber;

import burrow.chain.Context;
import burrow.chain.ContextHook;

import java.time.Instant;

public final class ChamberLifeCycleContext extends Context {
    public @interface Hook {
        ContextHook<Chamber> chamber = hook("chamber");
        ContextHook<Instant> creationTime = hook("creationTime");
    }
}
