package burrow.chain;

import burrow.chain.event.ThrowableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class Chain<C extends Context> extends Reactor<C> {
    private final List<Middleware<C>> middlewareList = new ArrayList<>();

    public static void runIfNotNull(@Nullable final Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    @NotNull
    public C apply(@NotNull final C context) {
        final var middleware = compose(middlewareList);
        middleware.accept(context, null);
        resolveQueue(context);

        return context;
    }

    @NotNull
    public Middleware<C> compose(@NotNull final List<Middleware<C>> middlewareList) {
        return (ctx, next) -> {
            dispatch(ctx, middlewareList, 0);
            runIfNotNull(next);
        };
    }

    private void dispatch(
        @NotNull final C context,
        @NotNull final List<Middleware<C>> middlewareList,
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

    private void resolveQueue(@NotNull final C context) {
        final var eventQueue = context.getEventQueue();
        if (eventQueue != null) {
            while (!eventQueue.isEmpty()) {
                trigger(eventQueue.poll(), context);
            }
        }
    }

    public void use(@NotNull final Middleware<C> middleware) {
        middlewareList.add(middleware);
    }

    public void use(@NotNull final Middleware.Pre<C> preMiddleware) {
        use((context, next) -> {
            preMiddleware.accept(context);
            runIfNotNull(next);
        });
    }

    public void use(@NotNull final Middleware.Post<C> postMiddleware) {
        use((context, next) -> {
            runIfNotNull(next);
            postMiddleware.accept(context);
        });
    }

    public void useFirst(@NotNull final Middleware<C> middleware) {
        middlewareList.addFirst(middleware);
    }

    public void useFirst(@NotNull final Middleware.Pre<C> preMiddleware) {
        useFirst((context, next) -> {
            preMiddleware.accept(context);
            runIfNotNull(next);
        });
    }

    public void useFirst(@NotNull final Middleware.Post<C> postMiddleware) {
        useFirst((context, next) -> {
            runIfNotNull(next);
            postMiddleware.accept(context);
        });
    }
}
