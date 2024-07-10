package burrow.chain;

import burrow.chain.event.ThrowableEvent;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class Chain<C extends Context> extends Reactor<C> {
    private final List<Middleware<C>> middlewareList = new ArrayList<>();

    public static void runIfNotNull(@Nullable final Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    @NonNull
    public C apply(@NonNull final C context) {
        final var middleware = compose(middlewareList);
        middleware.accept(context, null);
        resolveQueue(context);

        return context;
    }

    @NonNull
    public Middleware<C> compose(@NonNull final List<Middleware<C>> middlewareList) {
        return (ctx, next) -> {
            dispatch(ctx, middlewareList, 0);
            runIfNotNull(next);
        };
    }

    private void dispatch(
        @NonNull final C context,
        @NonNull final List<Middleware<C>> middlewareList,
        final int middlewareIndex
    ) {
        resolveQueue(context);

        if (middlewareIndex >= middlewareList.size()) {
            return;
        }

        if (Boolean.TRUE.equals(context.getTerminationFlag())) {
            return;
        }

        final var middleware = middlewareList.get(middlewareIndex);

        try {
            middleware.accept(context, () -> dispatch(context, middlewareList,
                middlewareIndex + 1));
        } catch (final Throwable throwable) {
            trigger(new ThrowableEvent(throwable), context);
        }
    }

    private void resolveQueue(@NonNull final C context) {
        final var eventQueue = context.getEventQueue();
        if (eventQueue != null) {
            while (!eventQueue.isEmpty()) {
                trigger(eventQueue.poll(), context);
            }
        }
    }

    public void use(@NonNull final Middleware<C> middleware) {
        middlewareList.add(middleware);
    }

    public void use(@NonNull final Middleware.Pre<C> preMiddleware) {
        use((context, next) -> {
            preMiddleware.accept(context);
            runIfNotNull(next);
        });
    }

    public void use(@NonNull final Middleware.Post<C> postMiddleware) {
        use((context, next) -> {
            runIfNotNull(next);
            postMiddleware.accept(context);
        });
    }

    public void useFirst(@NonNull final Middleware<C> middleware) {
        middlewareList.addFirst(middleware);
    }

    public void useFirst(@NonNull final Middleware.Pre<C> preMiddleware) {
        useFirst((context, next) -> {
            preMiddleware.accept(context);
            runIfNotNull(next);
        });
    }

    public void useFirst(@NonNull final Middleware.Post<C> postMiddleware) {
        useFirst((context, next) -> {
            runIfNotNull(next);
            postMiddleware.accept(context);
        });
    }
}
