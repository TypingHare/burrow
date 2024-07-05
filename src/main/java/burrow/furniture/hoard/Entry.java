package burrow.furniture.hoard;

import burrow.core.common.Values;
import burrow.furniture.hoard.exception.KeyNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an entry with a unique identifier and a set of key-value properties. This class
 * facilitates the storage and retrieval of properties associated with an entry.
 */
public final class Entry {
    /**
     * The unique identifier for this entry.
     */
    private final int id;

    /**
     * A map to store properties (key-value pairs) associated with this entry.
     */
    private final Map<String, String> properties = new HashMap<>();

    /**
     * Constructs a new Entry with the specified unique identifier.
     * @param id The unique identifier for this entry.
     */
    public Entry(int id) {
        this.id = id;
    }

    /**
     * Retrieves the unique identifier for this entry.
     * @return The unique identifier of this entry.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets a property (key-value pair) for this entry. If the property already exists, its value is
     * updated.
     * @param key   The key of the property to set.
     * @param value The value to associate with the key.
     */
    public <T> void set(@NonNull final String key, @NonNull final T value) {
        properties.put(key, value.toString());
    }

    public void unset(@NonNull final String key) {
        properties.remove(key);
    }

    public <T> void setIfAbsent(@NonNull final String key, @NonNull final T value) {
        properties.putIfAbsent(key, value.toString());
    }

    /**
     * Retrieves all properties associated with this entry.
     * @return A map containing all key-value pairs of properties associated with this entry.
     */
    @NonNull
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Retrieves the value of a property by its key.
     * @param key The key of the property to retrieve.
     * @return The value associated with the specified key.
     * @throws KeyNotFoundException If the key does not exist in the properties map.
     */
    @NonNull
    public String get(@NonNull final String key) {
        return Optional.ofNullable(properties.get(key))
            .orElseThrow(() -> new KeyNotFoundException(key));
    }

    @NonNull
    public String getNonNull(@NonNull final String key) {
        return Objects.requireNonNull(properties.get(key));
    }

    @Nullable
    public String getOrDefault(@NonNull final String key, final String defaultValue) {
        return Optional.ofNullable(properties.get(key)).orElse(defaultValue);
    }

    public boolean isTrue(@NonNull final String key) {
        return Values.Bool.isTrue(properties.get(key));
    }

    public boolean isFalse(@NonNull final String key) {
        return Values.Bool.isFalse(properties.get(key));
    }

    public int getInt(@NonNull final String key, final int defaultValue) {
        final String value = properties.get(key);
        return value == null ? defaultValue : Integer.parseInt(value);
    }
}
