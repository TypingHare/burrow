package burrow.chain.event;

import burrow.chain.Context;
import org.springframework.lang.NonNull;

public final class ThrowableEvent extends Event {
    private final Throwable throwable;

    public ThrowableEvent(@NonNull final Throwable throwable) {
        this.throwable = throwable;
    }

    public static void handler(
        @NonNull final Context context,
        @NonNull final ThrowableEvent event
    ) {
        context.setTerminationFlag(true);
        System.out.println(event.getThrowable().getLocalizedMessage());
    }

    @NonNull
    public Throwable getThrowable() {
        return throwable;
    }
}
