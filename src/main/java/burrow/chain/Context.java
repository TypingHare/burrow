package burrow.chain;

import burrow.chain.event.Event;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Context {
    protected final Map<String, Object> store = new HashMap<>();

    @Nullable
    public Object get(@NonNull final String key) {
        return store.get(key);
    }

    @Nullable
    public <T> T get(@NonNull final String key, @NonNull final Class<T> clazz) {
        return clazz.cast(get(key));
    }

    @NonNull
    public Object getOrDefault(@NonNull final String key, @NonNull final Object defaultValue) {
        return store.getOrDefault(key, defaultValue);
    }

    @NonNull
    public <T> T getOrDefault(
        @NonNull final String key, @NonNull final Class<T> clazz, @NonNull final T defaultValue) {
        final T value = get(key, clazz);
        return value == null ? defaultValue : value;
    }

    public void set(@NonNull final String key, @Nullable final Object value) {
        store.put(key, value);
    }

    public <R> void compute(
        @NonNull final String key,
        @NonNull final Function<Object, R> remappingFunction
    ) {
        store.compute(key, (k, v) -> remappingFunction.apply(v));
    }

    public <T> void compute(
        @NonNull final String key,
        @NonNull final Class<T> objectClass,
        @NonNull final Function<T, T> remappingFunction
    ) {
        store.compute(key, (k, v) -> remappingFunction.apply(objectClass.cast(v)));
    }

    @NonNull
    public Context shallowCopy() {
        final var newContext = new Context();
        for (final var entry : store.entrySet()) {
            newContext.set(entry.getKey(), entry.getValue());
        }

        return newContext;
    }

    @Nullable
    public List<Event> getEventList() {
        @SuppressWarnings("unchecked") final List<Event> eventList =
            (List<Event>) store.get(Key.EVENT_LIST);

        return eventList;
    }

    public void trigger(@NonNull final Event event) {
        @SuppressWarnings("unchecked") final List<Event> eventList =
            (List<Event>) store.computeIfAbsent(Key.EVENT_LIST, k -> new ArrayList<>());
        eventList.add(event);
    }

    public static final class Key {
        public static final String EVENT_LIST = "EVENT_LIST";
    }
}
