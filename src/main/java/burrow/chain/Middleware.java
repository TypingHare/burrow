package burrow.chain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@FunctionalInterface
public interface Middleware<C extends Context> extends BiConsumer<C, Runnable> {
    @Override
    void accept(@NotNull final C ctx, @Nullable final Runnable next);

    interface Pre<C extends Context> extends Consumer<C> {
    }

    interface Post<C extends Context> extends Consumer<C> {
    }
}
