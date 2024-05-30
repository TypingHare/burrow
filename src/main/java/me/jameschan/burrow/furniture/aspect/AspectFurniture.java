package me.jameschan.burrow.furniture.aspect;

import java.util.function.BiConsumer;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberShepherd;
import me.jameschan.burrow.kernel.context.RequestContext;
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

  public void beforeExecution(final BiConsumer<Chamber, RequestContext> listener) {
    getChamberShepherd().beforeExecution(listener);
  }

  public void afterExecution(final BiConsumer<Chamber, RequestContext> listener) {
    getChamberShepherd().afterExecution(listener);
  }
}
