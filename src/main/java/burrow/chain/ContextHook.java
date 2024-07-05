package burrow.chain;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.function.Function;

public class Hook<T> {
    private final String key;

    Hook(@NonNull final String key) {
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
}
