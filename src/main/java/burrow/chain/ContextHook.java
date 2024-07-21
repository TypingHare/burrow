package burrow.chain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class ContextHook<T> {
    private final String key;

    ContextHook(@NotNull final String key) {
        this.key = key;
    }

    public void set(@NotNull final Context context, @Nullable final T value) {
        context.set(key, value);
    }

    @Nullable
    public T get(@NotNull final Context context) {
        @SuppressWarnings("unchecked") final T value = (T) context.get(key);
        return value;
    }

    @NotNull
    public T getNotNull(@NotNull final Context context) {
        final T value = get(context);
        if (value == null) {
            throw new RuntimeException("Null context required not null: " + key);
        }

        return value;
    }

    @Nullable
    public T getOrDefault(@NotNull final Context context, @NotNull final T defaultValue) {
        @SuppressWarnings("unchecked") final T value =
            (T) context.getOrDefault(key, defaultValue);
        return value;
    }

    public void compute(
        @NotNull final Context context,
        @NotNull final Function<T, T> remappingFunction
    ) {
        set(context, remappingFunction.apply(get(context)));
    }

    public T computeIfAbsent(
        @NotNull final Context context,
        @NotNull final Supplier<T> supplier
    ) {
        return context.computeIfAbsent(key, supplier);
    }
}
