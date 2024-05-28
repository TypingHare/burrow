package me.jameschan.burrow.kernel.furniture;

public class FurnitureNotFoundException extends ClassNotFoundException {
  public FurnitureNotFoundException(final String furnitureName) {
    super(String.format("Furniture not found: %s", furnitureName));
  }
}
