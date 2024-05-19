package me.jameschan.burrow.furniture.builtin.furniture;

import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.furniture.Furniture;

public class FurnitureFurniture extends Furniture {
  public FurnitureFurniture(final Chamber chamber) {
    super(chamber);

    registerCommand(FurnitureCommand.class);
  }
}
