package me.jameschan.burrow.kernel;

import me.jameschan.burrow.kernel.context.ChamberContext;
import org.springframework.lang.NonNull;

public abstract class ChamberModule {
  protected final Chamber chamber;
  protected final ChamberContext context;

  public ChamberModule(@NonNull final Chamber chamber) {
    this.chamber = chamber;
    this.context = chamber.getContext();
  }

  @NonNull
  public Chamber getChamber() {
    return chamber;
  }

  @NonNull
  public ChamberContext getContext() {
    return chamber.getContext();
  }
}
