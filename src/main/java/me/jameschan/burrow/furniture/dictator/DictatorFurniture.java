package me.jameschan.burrow.furniture.dictator;

import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;

@BurrowFurniture
public class DictatorFurniture extends Furniture {
  public DictatorFurniture(final Chamber chamber) {
    super(chamber);
  }

  @Override
  public void init() {
    registerCommand(ChamberNewCommand.class);
  }
}
