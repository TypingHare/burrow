package me.jameschan.burrow.chamber;

import me.jameschan.burrow.context.Context;

public class ChamberBased {
  private final Chamber chamber;

  public ChamberBased(final Chamber chamber) {
    this.chamber = chamber;
  }

  public Chamber getChamber() {
    return chamber;
  }

  public Context getContext() {
    return chamber.getContext();
  }
}
