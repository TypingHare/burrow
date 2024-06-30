package burrow.furniture.aspectcore;

import burrow.chain.Middleware;
import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberShepherd;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import org.springframework.lang.NonNull;

@BurrowFurniture(
    simpleName = "Aspect Core",
    description = "t"
)
public class AspectCoreFurniture extends Furniture {
    public AspectCoreFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @NonNull
    public ChamberShepherd getChamberShepherd() {
        return context.getChamber().getApplicationContext().getBean(ChamberShepherd.class);
    }

    public ChamberShepherd.ProcessChain getProcessChain() {
        return getChamberShepherd().getProcessChain();
    }

    public void onBeforeExecution(
        @NonNull final Middleware.Pre<ChamberShepherd.ProcessContext> middleware
    ) {
        getProcessChain().pre.use(middleware);
    }

    public void afterExecution(
        @NonNull final Middleware.Post<ChamberShepherd.ProcessContext> middleware
    ) {
        getProcessChain().post.use(middleware);
    }
}
