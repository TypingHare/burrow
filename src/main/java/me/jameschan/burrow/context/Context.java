package me.jameschan.burrow.context;

import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.config.Config;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a context for storing key-value pairs of data. It provides methods to set
 * and retrieve data by key.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Context {
    /**
     * The available keys to context. Refer to the context table in documentation for more
     * information.
     */
    public final static class Key {
        public static final String ROOT_DIR = "ROOT_DIR";
        public static final String CHAMBER = "CHAMBER";
        public static final String CONFIG_FILE_PATH = "CONFIG_FILE_PATH";
        public static final String CONFIG = "CONFIG";
    }

    // Internal map to store data
    private final Map<String, Object> data = new HashMap<>();

    /**
     * Set a value for the given key in the context.
     * @param key   The key to set the value for.
     * @param value The value to set.
     */
    public void set(final String key, final Object value) {
        data.put(key, value);
    }

    /**
     * Get the value associated with the given key from the context.
     * @param key   The key to get the value for.
     * @param clazz The class of the value to be retrieved.
     * @param <T>   The type of the value to be retrieved.
     * @return The value associated with the key, or null if the key is not present.
     */
    public <T> T get(final String key, final Class<? extends T> clazz) {
        return clazz.cast(data.get(key));
    }

    /**
     * Get the value associated with the given key from the context as a String.
     * @param key The key to get the value for.
     * @return The value associated with the key as a String, or null if the key is not present.
     */
    public String get(final String key) {
        return get(key, String.class);
    }

    public Path getRootDir() {
        return get(Key.ROOT_DIR, Path.class);
    }

    public Chamber getChamber() {
        return get(Key.CHAMBER, Chamber.class);
    }

    public Path getConfigFilePath() {
        return get(Key.CONFIG_FILE_PATH, Path.class);
    }

    public Config getConfig() {
        return get(Key.CONFIG, Config.class);
    }


}
