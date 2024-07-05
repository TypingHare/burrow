package burrow.chain;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class ContextHook<T> {
    private final String key;

    ContextHook(@NonNull final String key) {
        this.key = key;
    }

    public void set(@NonNull final Context context, @Nullable final T value) {
        context.set(key, value);
    }

    @Nullable
    public T get(@NonNull final Context context) {
        @SuppressWarnings("unchecked") final T value = (T) context.get(key);
        return value;
    }

    @NonNull
    public T getNonNull(@NonNull final Context context) {
        final T value = get(context);
        if (value == null) {
            throw new RuntimeException("Null context required NonNull: " + key);
        }

        return value;
    }

    @Nullable
    public T getOrDefault(@NonNull final Context context, @NonNull final T defaultValue) {
        @SuppressWarnings("unchecked") final T value =
            (T) context.getOrDefault(key, defaultValue);
        return value;
    }

    public void compute(
        @NonNull final Context context,
        @NonNull final Function<T, T> remappingFunction
    ) {
        set(context, remappingFunction.apply(get(context)));
    }

    public T computeIfAbsent(
        @NonNull final Context context,
        @NonNull final Supplier<T> supplier
    ) {
        return context.computeIfAbsent(key, supplier);
    }
}
