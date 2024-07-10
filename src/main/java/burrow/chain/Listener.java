package burrow.chain;

import burrow.chain.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface Listener<C extends Context, E extends Event> extends BiConsumer<C, E> {
    default void accept(@NotNull final C context, @NotNull final Event event) {
        @SuppressWarnings("unchecked") final E e = (E) event;
        accept(context, e);
    }
}
