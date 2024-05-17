package me.jameschan.burrow.hoard;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents an entry with a unique identifier and a set of key-value properties. This class
 * facilitates the storage and retrieval of properties associated with an entry.
 */
public class Entry {
  /** The unique identifier for this entry. */
  private final int id;

  /** A map to store properties (key-value pairs) associated with this entry. */
  private final Map<String, String> properties = new HashMap<>();

  /**
   * Constructs a new Entry with the specified unique identifier.
   *
   * @param id The unique identifier for this entry.
   */
  public Entry(int id) {
    this.id = id;
  }

  /**
   * Retrieves the unique identifier for this entry.
   *
   * @return The unique identifier of this entry.
   */
  public int getId() {
    return id;
  }

  /**
   * Sets a property (key-value pair) for this entry. If the property already exists, its value is
   * updated.
   *
   * @param key The key of the property to set.
   * @param value The value to associate with the key.
   */
  public void set(final String key, final String value) {
    properties.put(key, value);
  }

  /**
   * Retrieves all properties associated with this entry.
   *
   * @return A map containing all key-value pairs of properties associated with this entry.
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Retrieves the value of a property by its key.
   *
   * @param key The key of the property to retrieve.
   * @return The value associated with the specified key.
   * @throws KeyNotFoundException If the key does not exist in the properties map.
   */
  public String get(final String key) {
    return Optional.ofNullable(properties.get(key))
        .orElseThrow(() -> new KeyNotFoundException(key));
  }
}
