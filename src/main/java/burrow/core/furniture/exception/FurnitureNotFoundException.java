package burrow.core.furniture.exception;

import org.jetbrains.annotations.NotNull;

public final class FurnitureNotFoundException extends RuntimeException {
    public FurnitureNotFoundException(@NotNull final String furnitureFullName) {
        super("Furniture not found: " + furnitureFullName);
    }
}
