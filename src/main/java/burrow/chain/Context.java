package burrow.chain;

import burrow.chain.event.Event;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

public class Context {
    protected final Map<String, Object> store = new HashMap<>();

    protected static <T> ContextHook<T> hook(@NonNull final String key) {
        return new ContextHook<>(key);
    }

    public void set(@NonNull final String key, @Nullable final Object value) {
        if (value == null) {
            store.remove(key);
        } else {
            store.put(key, value);
        }
    }

    @Nullable
    public Object get(@NonNull final String key) {
        return store.get(key);
    }

    @NonNull
    public Object getOrDefault(@NonNull final String key, @NonNull final Object defaultValue) {
        return store.getOrDefault(key, defaultValue);
    }

    public <R> void compute(
        @NonNull final String key,
        @NonNull final Function<R, R> function
    ) {
        @SuppressWarnings("unchecked") final R value = (R) store.get(key);
        store.put(key, function.apply(value));
    }

    public <R> R computeIfAbsent(
        @NonNull final String key,
        @NonNull final Supplier<R> supplier
    ) {
        @SuppressWarnings("unchecked") final R value =
            (R) store.computeIfAbsent(key, (k) -> supplier.get());
        return value;
    }

    public void trigger(@NonNull final Event event) {
        final var eventQueue = Hook.eventQueue.computeIfAbsent(this, LinkedList::new);
        eventQueue.add(event);
    }

    @NonNull
    public Context shallowCopy() {
        final var newContext = new Context();
        newContext.store.putAll(store);
        return newContext;
    }

    public @interface Hook {
        ContextHook<Queue<Event>> eventQueue = hook("EVENT_QUEUE");
        ContextHook<Boolean> termination = hook("TERMINATION");
    }
}
