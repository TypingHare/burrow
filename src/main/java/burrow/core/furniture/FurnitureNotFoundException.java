package burrow.core.furniture;

import org.springframework.lang.NonNull;

public final class FurnitureNotFoundException extends RuntimeException {
    public FurnitureNotFoundException(@NonNull final String furnitureFullName) {
        super("Furniture not found: " + furnitureFullName);
    }
}
