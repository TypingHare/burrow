package burrow.chain;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Context {
    private final Map<String, Object> store = new HashMap<>();

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

    public void computeIfAbsent(
        @NonNull final String key,
        @NonNull final BiFunction<String, Object, Object> remappingFunction
    ) {
        store.compute(key, remappingFunction);
    }

    public <T> void compute(
        @NonNull final String key,
        @NonNull final Class<T> objectClass,
        @NonNull final Function<T, T> remappingFunction
    ) {
        store.compute(key, (k, v) -> remappingFunction.apply(objectClass.cast(v)));
    }
}
