package burrow.chain;

import burrow.chain.event.Event;
import org.springframework.lang.NonNull;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface Listener<C extends Context, E extends Event> extends BiConsumer<C, E> {
    default void accept(@NonNull final C context, @NonNull final Event event) {
        @SuppressWarnings("unchecked")
        final E e = (E) event;
        accept(context, e);
    }
}
