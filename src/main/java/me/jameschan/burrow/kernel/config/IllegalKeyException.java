package me.jameschan.burrow.kernel.config;

/**
 * An exception that is thrown when an attempt is made to use a key that does not exist in the
 * predefined key set.
 */
public class IllegalKeyException extends RuntimeException {
  /**
   * Constructs an {@code IllegalKeyException} with the detail message constructed from the illegal
   * key.
   *
   * @param key The key that triggered this exception.
   */
  public IllegalKeyException(final String key) {
    super(String.format("Key does not exist in the allowed key set: %s", key));
  }
}
