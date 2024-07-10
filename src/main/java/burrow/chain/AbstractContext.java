package burrow.chain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractContext {
    protected final Map<String, Object> store = new HashMap<>();

    public void set(@NotNull final String key, @Nullable final Object value) {
        if (value == null) {
            store.remove(key);
        } else {
            store.put(key, value);
        }
    }

    @Nullable
    public Object get(@NotNull final String key) {
        return store.get(key);
    }

    @NotNull
    public Object getOrDefault(@NotNull final String key, @NotNull final Object defaultValue) {
        return store.getOrDefault(key, defaultValue);
    }

    public <R> void compute(
        @NotNull final String key,
        @NotNull final Function<R, R> function
    ) {
        @SuppressWarnings("unchecked") final R value = (R) store.get(key);
        store.put(key, function.apply(value));
    }

    public <R> R computeIfAbsent(
        @NotNull final String key,
        @NotNull final Supplier<R> supplier
    ) {
        @SuppressWarnings("unchecked") final R value =
            (R) store.computeIfAbsent(key, (k) -> supplier.get());
        return value;
    }

    @NotNull
    public Context shallowCopy() {
        final var newContext = new Context();
        newContext.store.putAll(store);
        return newContext;
    }
}
