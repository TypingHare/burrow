package burrow.core.chamber;

import burrow.chain.Chain;
import org.springframework.lang.NonNull;

import java.time.Instant;

public class CreateChamberChain extends Chain<ChamberLifeCycleContext> {
    @NonNull
    public ChamberLifeCycleContext apply(
        @NonNull final Chamber chamber,
        @NonNull final Instant creationTime
    ) {
        final var context = new ChamberLifeCycleContext();
        ChamberLifeCycleContext.Hook.chamber.set(context, chamber);
        ChamberLifeCycleContext.Hook.creationTime.set(context, creationTime);

        return super.apply(context);
    }
}
