package me.jameschan.burrow.furniture.keyvalue;

import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;

@BurrowFurniture
public class KeyValueFurniture extends Furniture {
  public KeyValueFurniture(final Chamber chamber) {
    super(chamber);
  }
}
