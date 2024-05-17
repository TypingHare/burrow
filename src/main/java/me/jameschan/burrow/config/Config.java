package me.jameschan.burrow.config;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A configuration class that stores settings or parameters. Only keys present in provided key set
 * are allowed, ensuring the configuration contains only predefined keys.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Config {
    /**
     * Default configuration keys.
     */
    public final static class Key {
        public static final String APP_NAME = "app.name";
        public static final String APP_VERSION = "app.version";
        public static final String APP_DEBUG = "app.debug";
        public static final String FURNITURE_PATH = "furniture.path";
        public static final String FURNITURE_LIST = "furniture.list";
    }

    public Config() {
        addKey(Key.APP_NAME);
        addKey(Key.APP_VERSION);
        addKey(Key.APP_DEBUG);
        addKey(Key.FURNITURE_PATH);
        addKey(Key.FURNITURE_LIST);
    }

    /**
     * Set containing allowed keys for configuration.
     */
    private final Set<String> keySet = new HashSet<>();

    /**
     * Internal storage for configuration data.
     */
    private final Map<String, String> data = new HashMap<>();

    /**
     * Adds a key to the set of allowed keys for configuration. Only keys present in this set are
     * allowed, ensuring that the configuration contains only predefined keys.
     * @param key The key to be added to the set of allowed keys.
     */
    public void addKey(final String key) {
        keySet.add(key);
    }

    /**
     * Retrieves the value associated with the specified key.
     * @param key The key whose associated value is to be returned.
     * @return The value to which the specified key is mapped, or {@code null} if this map contains
     * no mapping for the key.
     */
    public String get(final String key) {
        return data.get(key);
    }

    /**
     * Inserts or updates the value associated with the specified key. Only keys present in the
     * keySet are allowed; attempts to insert other keys will result in an IllegalKeyException.
     * @param key   The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     * @throws IllegalKeyException If the key is not present in the predefined set of allowed keys.
     */
    public void set(final String key, final String value) {
        if (!keySet.contains(key)) {
            throw new IllegalKeyException(key);
        }

        data.put(key, value);
    }

    /**
     * Retrieves the internal data structure containing the configuration data.
     * @return A map representing the configuration data, where keys are mapped to their
     * corresponding values.
     */
    public Map<String, String> getData() {
        return data;
    }
}
