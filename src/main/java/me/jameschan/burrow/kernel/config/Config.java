package me.jameschan.burrow.kernel.config;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberModule;
import me.jameschan.burrow.kernel.common.Types;
import me.jameschan.burrow.kernel.context.ChamberContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A configuration class that stores settings or parameters. Only predefined keys are allowed,
 * ensuring the configuration contains only valid keys.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Config extends ChamberModule {
  /** Config file name; */
  public static final String CONFIG_FILE_NAME = "config.json";

  /** Set containing allowed keys for configuration. */
  private final Set<String> allowedKeySet = new HashSet<>();

  /** Internal storage for configuration data. */
  private final Map<String, String> store = new HashMap<>();

  /** Constructs a new Config instance with default allowed keys. */
  public Config(final Chamber chamber) {
    super(chamber);
    addAllowedKey(Key.CHAMBER_NAME);
    addAllowedKey(Key.CHAMBER_VERSION);
    addAllowedKey(Key.FURNITURE_LIST);
  }

  /**
   * Adds a key to the set of allowed keys for configuration. Only keys present in this set are
   * permitted, ensuring the configuration contains only predefined keys.
   *
   * @param key The key to be added to the set of allowed keys.
   */
  public void addAllowedKey(final String key) {
    allowedKeySet.add(key);
  }

  /**
   * Retrieves the value associated with the specified key.
   *
   * @param key The key whose associated value is to be returned.
   * @return The value associated with the specified key, or {@code null} if no value is mapped to
   *     the key.
   */
  public String get(final String key) {
    return store.get(key);
  }

  /**
   * Inserts or updates the value associated with the specified key. Only keys present in the
   * allowed key set are permitted; attempts to insert other keys will result in an
   * IllegalKeyException.
   *
   * @param key The key with which the specified value is to be associated.
   * @param value The value to be associated with the specified key.
   * @throws IllegalKeyException If the key is not present in the predefined set of allowed keys.
   */
  public void set(final String key, final String value) {
    if (!allowedKeySet.contains(key)) {
      throw new IllegalKeyException(key);
    }
    store.put(key, value);
  }

  public void loadFromFile() throws ConfigFileNotFoundException {
    final var filePath = getContext().getRootDir().resolve(CONFIG_FILE_NAME).normalize();
    context.set(ChamberContext.Key.CONFIG_FILE, filePath.toFile());

    if (!filePath.toFile().exists()) {
      throw new ConfigFileNotFoundException(filePath.toString());
    }

    try {
      final var config = context.getConfig();
      final var content = Files.readString(filePath);
      final Map<String, String> map = new Gson().fromJson(content, Types.STRING_STRING_MAP);
      map.keySet().forEach(config::addAllowedKey);
      map.forEach(config::set);
      context.set(ChamberContext.Key.CONFIG, config);
    } catch (final Exception ex) {
      throw new RuntimeException("Fail to load config: " + filePath, ex);
    }
  }

  public void saveToFile() {
    final var content = new Gson().toJson(getStore());

    try {
      Files.write(context.getConfigFile().toPath(), content.getBytes());
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Retrieves the internal data structure containing the configuration data.
   *
   * @return A map representing the configuration data, where keys are mapped to their corresponding
   *     values.
   */
  public Map<String, String> getStore() {
    return store;
  }

  /** Default configuration keys. */
  public static final class Key {
    public static final String CHAMBER_NAME = "chamber.name";
    public static final String CHAMBER_VERSION = "chamber.version";
    public static final String FURNITURE_LIST = "furniture.list";
  }
}
