package burrow.core.furniture;

public class InvalidFurnitureClassException extends Exception {
    public InvalidFurnitureClassException(final String furnitureName) {
        super(String.format("Invalid furniture: %s", furnitureName));
    }
}
