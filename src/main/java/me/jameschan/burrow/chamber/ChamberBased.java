package me.jameschan.burrow.chamber;

import me.jameschan.burrow.context.ChamberContext;

public class ChamberBased {
  private final Chamber chamber;

  public ChamberBased(final Chamber chamber) {
    this.chamber = chamber;
  }

  public Chamber getChamber() {
    return chamber;
  }

  public ChamberContext getContext() {
    return chamber.getContext();
  }
}
