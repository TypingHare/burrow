package burrow.core.chamber;

import burrow.chain.Chain;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class CreateChamberChain extends Chain<ChamberLifeCycleContext> {
    @NotNull
    public ChamberLifeCycleContext apply(
        @NotNull final Chamber chamber,
        @NotNull final Instant creationTime
    ) {
        final var context = new ChamberLifeCycleContext();
        context.setChamber(chamber);
        context.setCreationTime(creationTime);

        return super.apply(context);
    }
}
