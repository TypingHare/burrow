package burrow.core.furniture;

import org.springframework.lang.NonNull;

public class InvalidFurnitureClassException extends Exception {
    public InvalidFurnitureClassException(
        @NonNull final String furnitureName,
        @NonNull final String cause
    ) {
        super(String.format("Invalid furniture: %s, %s", furnitureName, cause));
    }
}
