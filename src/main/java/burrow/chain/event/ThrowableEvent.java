package burrow.chain.event;

import burrow.chain.Context;
import org.jetbrains.annotations.NotNull;

public final class ThrowableEvent extends Event {
    private final Throwable throwable;

    public ThrowableEvent(@NotNull final Throwable throwable) {
        this.throwable = throwable;
    }

    public static void handler(
        @NotNull final Context context,
        @NotNull final ThrowableEvent event
    ) {
        context.setTerminationFlag(true);
        System.out.println(event.getThrowable().getLocalizedMessage());
    }

    @NotNull
    public Throwable getThrowable() {
        return throwable;
    }
}
