package burrow.core.furniture.exception;

import org.jetbrains.annotations.NotNull;

public final class InvalidFurnitureClassException extends Exception {
    public InvalidFurnitureClassException(
        @NotNull final String furnitureName,
        @NotNull final String cause
    ) {
        super(String.format("Invalid furniture: %s, %s", furnitureName, cause));
    }
}
