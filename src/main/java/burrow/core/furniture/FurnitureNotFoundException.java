package burrow.core.furniture;

public class FurnitureNotFoundException extends RuntimeException {
    public FurnitureNotFoundException(final String furnitureFullName) {
        super("Furniture not found: " + furnitureFullName);
    }
}
