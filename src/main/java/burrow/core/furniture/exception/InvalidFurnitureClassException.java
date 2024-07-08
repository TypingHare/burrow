package burrow.core.furniture.exception;

import org.springframework.lang.NonNull;

public final class InvalidFurnitureClassException extends Exception {
    public InvalidFurnitureClassException(
        @NonNull final String furnitureName,
        @NonNull final String cause
    ) {
        super(String.format("Invalid furniture: %s, %s", furnitureName, cause));
    }
}
