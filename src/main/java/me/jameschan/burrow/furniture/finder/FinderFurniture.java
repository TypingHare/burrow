package me.jameschan.burrow.furniture.finder;

import me.jameschan.burrow.furniture.keyvalue.KeyValueFurniture;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;

@BurrowFurniture(
    dependencies = {KeyValueFurniture.class},
    description = "The core of a finder chamber.")
public class FinderFurniture extends Furniture {
  public FinderFurniture(final Chamber chamber) {
    super(chamber);
  }

  @Override
  public void init() {
    registerCommand(DefaultCommand.class);
    registerCommand(AddCommand.class);
    registerCommand(SearchCommand.class);
  }
}
