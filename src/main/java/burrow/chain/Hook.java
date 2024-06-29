package burrow.chain;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.function.Function;

public class Hook<T> {
    private final String key;
    private final Class<T> clazz;

    private Hook(
        @NonNull final String key,
        @Nullable final Class<T> clazz
    ) {
        this.key = key;
        this.clazz = clazz;
    }

    public static <T> Hook<T> of(@NonNull final String key, @Nullable final Class<T> clazz) {
        return new Hook<>(key, clazz);
    }

    public static <T> Hook<T> of(@NonNull final String key) {
        return new Hook<>(key, null);
    }

    public void set(
        @NonNull final Context context,
        @NonNull final T value
    ) {
        context.set(key, value);
    }

    public T get(@NonNull final Context context) {
        if (clazz == null) {
            @SuppressWarnings("unchecked") final T value = (T) context.get(key);
            return value;
        } else {
            return clazz.cast(context.get(key));
        }
    }

    public T getOrDefault(@NonNull final Context context, @NonNull final T defaultValue) {
        if (clazz == null) {
            @SuppressWarnings("unchecked") final T value =
                (T) context.getOrDefault(key, defaultValue);
            return value;
        } else {
            return context.getOrDefault(key, clazz, defaultValue);
        }
    }

    public void compute(
        @NonNull final Context context,
        @NonNull final Function<T, T> remappingFunction
    ) {
        if (clazz == null) {
            set(context, remappingFunction.apply(get(context)));
        } else {
            context.compute(key, clazz, remappingFunction);
        }
    }
}
