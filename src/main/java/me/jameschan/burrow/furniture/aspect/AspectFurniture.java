package me.jameschan.burrow.furniture.aspect;

import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberShepherd;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;

@BurrowFurniture
public class AspectFurniture extends Furniture {
  public AspectFurniture(final Chamber chamber) {
    super(chamber);
  }

  public ChamberShepherd getChamberShepherd() {
    return context.getChamber().getApplicationContext().getBean(ChamberShepherd.class);
  }
}
