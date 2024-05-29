package me.jameschan.burrow.furniture.scheduler;

import me.jameschan.burrow.furniture.aspect.AspectFurniture;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;

@BurrowFurniture(dependencies = {AspectFurniture.class})
public class SchedulerFurniture extends Furniture {
  public SchedulerFurniture(final Chamber chamber) {
    super(chamber);
  }
}
