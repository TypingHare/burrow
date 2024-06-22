package burrow.chain;

import org.springframework.lang.NonNull;

import java.util.function.Function;

public class Hook<T> {
    public static <T> Hook<T> of(
        @NonNull final String key,
        @NonNull final Class<T> clazz
    ) {
        return new Hook<>(key, clazz);
    }

    private final String key;
    private final Class<T> clazz;

    private Hook(
        @NonNull final String key,
        @NonNull final Class<T> clazz
    ) {
        this.key = key;
        this.clazz = clazz;
    }

    public void set(
        @NonNull final Context context,
        @NonNull final Object value
    ) {
        context.set(key, value);
    }

    public T get(@NonNull final Context context) {
        return clazz.cast(context.get(key));
    }

    public T getOrDefault(@NonNull final Context context, @NonNull final T defaultValue) {
        return context.getOrDefault(key, clazz, defaultValue);
    }

    public void compute(
        @NonNull final Context context,
        @NonNull final Function<T, T> remappingFunction
    ) {
        context.compute(key, clazz, remappingFunction);
    }
}
