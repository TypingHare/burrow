package burrow.core.entry;

/**
 * Exception thrown when an attempt is made to access a key in an Entry that does not exist in its
 * properties map.
 */
public class KeyNotFoundException extends RuntimeException {
    /**
     * Constructs a KeyNotFoundException with a detail message constructed from the specified key
     * indicating that the key does not exist.
     * @param key The key that was attempted to be accessed but does not exist.
     */
    public KeyNotFoundException(final String key) {
        super(String.format("Key does not exist: %s", key));
    }
}
