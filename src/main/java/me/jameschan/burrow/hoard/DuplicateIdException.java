package me.jameschan.burrow.hoard;

public class DuplicateIdException extends RuntimeException {
    public DuplicateIdException(final int id) {
        super("Duplicate entry id: " + id);
    }
}
