package me.jameschan.burrow.furniture.builtin.chambermanagement;

import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.chamber.ChamberManager;
import me.jameschan.burrow.furniture.Furniture;
import me.jameschan.burrow.furniture.annotation.BurrowFurniture;

@BurrowFurniture
public class ChamberManagementFurniture extends Furniture {
  private final ChamberManager chamberManager;

  public ChamberManagementFurniture(final Chamber chamber) {
    super(chamber);

    chamberManager = chamber.getApplicationContext().getBean(ChamberManager.class);
  }

  public ChamberManager getChamberManager() {
    return chamberManager;
  }
}
