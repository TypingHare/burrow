package me.jameschan.burrow.kernel.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class representing a generic context for storing and retrieving key-value pairs. This
 * class provides a basic implementation for setting and getting values using a map.
 */
public abstract class Context {

  /** Internal map to store key-value pairs. */
  protected final Map<String, Object> data = new HashMap<>();

  /**
   * Sets a value for the given key in the context.
   *
   * @param key The key to set the value for. Must not be null.
   * @param value The value to set. Can be any object, including null.
   */
  public void set(final String key, final Object value) {
    data.put(key, value);
  }

  /**
   * Retrieves the value associated with the given key from the context.
   *
   * @param key The key to get the value for. Must not be null.
   * @param clazz The class of the value to be retrieved. Must not be null.
   * @param <T> The type of the value to be retrieved.
   * @return The value associated with the key, cast to the specified type, or null if the key is
   *     not present.
   * @throws RuntimeException if the value associated with the key cannot be cast to the specified
   *     type.
   */
  public <T> T get(final String key, final Class<? extends T> clazz) {
    try {
      return clazz.cast(data.get(key));
    } catch (final ClassCastException ex) {
      throw new RuntimeException(
          String.format(
              "The context value associated with key \"%s\" cannot be cast to: %s",
              key, clazz.getName()),
          ex);
    }
  }
}
