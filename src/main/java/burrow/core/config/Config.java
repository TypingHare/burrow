package burrow.core.config;

import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberModule;
import burrow.core.chamber.ChamberContext;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A configuration class that stores settings or parameters. Only predefined keys are allowed,
 * ensuring the configuration contains only valid keys.
 */
public class Config extends ChamberModule {
    /**
     * Config file name;
     */
    public static final String CONFIG_FILE_NAME = "config.json";

    /**
     * Set containing allowed keys for configuration.
     */
    private final Set<String> allowedKeySet = new HashSet<>();

    /**
     * Internal storage for configuration data.
     */
    private final Map<String, String> store = new HashMap<>();

    /**
     * Denotes whether the config has been changed.
     */
    private boolean hasChanged = false;

    /**
     * Constructs a new Config instance with default allowed keys.
     */
    public Config(final Chamber chamber) {
        super(chamber);

        addAllowedKey(Key.CHAMBER_NAME);
        addAllowedKey(Key.CHAMBER_VERSION);
        addAllowedKey(Key.CHAMBER_DESCRIPTION);
        addAllowedKey(Key.CHAMBER_FURNITURE_LIST);

        setIfAbsent(Key.CHAMBER_NAME, "");
        setIfAbsent(Key.CHAMBER_VERSION, "1.0.0");
        setIfAbsent(Key.CHAMBER_DESCRIPTION, "No description.");
        setIfAbsent(Key.CHAMBER_FURNITURE_LIST, "");
    }

    /**
     * Retrieves the internal data structure containing the configuration data.
     * @return A map representing the configuration data, where keys are mapped to their
     * corresponding values.
     */
    public Map<String, String> getStore() {
        return store;
    }

    /**
     * Adds a key to the set of allowed keys for configuration. Only keys present in this set are
     * permitted, ensuring the configuration contains only predefined keys.
     * @param key The key to be added to the set of allowed keys.
     */
    public void addAllowedKey(final String key) {
        allowedKeySet.add(key);
    }

    /**
     * Retrieves the value associated with the specified key.
     * @param key The key whose associated value is to be returned.
     * @return The value associated with the specified key, or {@code null} if no value is mapped to
     * the key.
     */
    @Nullable
    public String get(@NonNull final String key) {
        return store.get(key);
    }

    @NonNull
    public String getNonNull(@NonNull final String key) {
        final var value = store.get(key);
        if (value == null) {
            throw new RuntimeException("Missing required config key: " + key);
        }

        return value;
    }

    /**
     * Inserts or updates the value associated with the specified key. Only keys present in the
     * allowed key set are permitted; attempts to insert other keys will result in an
     * IllegalKeyException.
     * @param key   The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     * @throws IllegalKeyException If the key is not present in the predefined set of allowed keys.
     */
    public <T> void set(@NonNull final String key, @NonNull final T value) {
        if (!allowedKeySet.contains(key)) {
            throw new IllegalKeyException(key);
        }

        final var originalValue = store.get(key);
        final var newValue = value.toString();
        if (!Objects.equals(newValue, originalValue)) {
            hasChanged = true;
        }

        store.put(key, newValue);
    }

    public <T> void setIfAbsent(@NonNull final String key, @NonNull final T value) {
        if (!allowedKeySet.contains(key)) {
            throw new IllegalKeyException(key);
        }

        store.putIfAbsent(key, value.toString());
    }

    public void loadFromFile() throws ConfigFileNotFoundException {
        final var context = getChamberContext();
        final var rootPath = ChamberContext.Hook.rootPath.getNonNull(context);
        final var filePath = rootPath.resolve(CONFIG_FILE_NAME).normalize();
        ChamberContext.Hook.configPath.set(context, filePath);

        if (!filePath.toFile().exists()) {
            throw new ConfigFileNotFoundException(filePath.toString());
        }

        try {
            final var configMap = loadFromConfigFile(filePath);
            configMap.keySet().forEach(this::addAllowedKey);
            configMap.forEach(this::set);
            ChamberContext.Hook.config.set(context, this);
            hasChanged = false;
        } catch (final Exception ex) {
            throw new RuntimeException("Fail to load config: " + filePath, ex);
        }
    }

    public void saveToFile() {
        if (!hasChanged) {
            return;
        }

        final var content = new Gson().toJson(getStore());
        final var configPath = ChamberContext.Hook.configPath.getNonNull(getChamberContext());

        try {
            Files.write(configPath, content.getBytes());
        } catch (final IOException ex) {
            throw new RuntimeException("Fail to save config", ex);
        }
    }

    @NonNull
    public static Map<String, String> loadFromConfigFile(
        @NonNull final Path filePath) throws IOException {
        final var content = Files.readString(filePath);
        return new Gson().fromJson(content, new TypeToken<Map<String, String>>() {
        }.getType());
    }

    /**
     * Default configuration keys.
     */
    public @interface Key {
        String CHAMBER_NAME = "chamber.name";
        String CHAMBER_VERSION = "chamber.version";
        String CHAMBER_DESCRIPTION = "chamber.description";
        String CHAMBER_FURNITURE_LIST = "chamber.furniture_list";
    }
}
