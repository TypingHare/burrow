package me.jameschan.burrow.furniture.finder;

import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;

@BurrowFurniture(description = "The core of a finder chamber.")
public class FinderFurniture extends Furniture {
  public FinderFurniture(final Chamber chamber) {
    super(chamber);
  }
}
