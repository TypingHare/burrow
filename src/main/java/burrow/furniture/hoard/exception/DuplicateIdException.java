package burrow.furniture.hoard.exception;

public final class DuplicateIdException extends RuntimeException {
    public DuplicateIdException(final int id) {
        super("Duplicate entry id: " + id);
    }
}
