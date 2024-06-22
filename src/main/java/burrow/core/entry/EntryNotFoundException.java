package burrow.core.entry;

/**
 * Exception thrown when an attempt is made to access an entry that does not exist within the
 * system. This can occur when querying for an entry by an identifier that is not present in the
 * entry registry.
 */
public class EntryNotFoundException extends RuntimeException {
  /**
   * Constructs a new {@code EntryNotFoundException} with the specified detail message constructed
   * using the provided entry ID.
   *
   * @param id The unique identifier of the entry that was not found.
   */
  public EntryNotFoundException(final Integer id) {
    super(String.format("Entry with such ID not found: %d", id));
  }
}
