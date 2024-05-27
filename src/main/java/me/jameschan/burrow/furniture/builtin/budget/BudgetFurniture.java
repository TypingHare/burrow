package me.jameschan.burrow.furniture.builtin.budget;

import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.furniture.Furniture;
import me.jameschan.burrow.furniture.annotation.BurrowFurniture;
import me.jameschan.burrow.furniture.builtin.keyvalue.KeyValueFurniture;
import me.jameschan.burrow.furniture.builtin.time.TimeFurniture;
import me.jameschan.burrow.hoard.Entry;

@BurrowFurniture(dependencies = {KeyValueFurniture.class, TimeFurniture.class})
public class BudgetFurniture extends Furniture {
  public BudgetFurniture(final Chamber chamber) {
    super(chamber);
  }

  @Override
  public void onCreateEntry(final Entry entry) {
    final var value = entry.get(KeyValueFurniture.EntryKey.VALUE);
    try {
      final var amount = Float.parseFloat(value);
      entry.set(KeyValueFurniture.EntryKey.VALUE, String.format("%.2f", amount));
    } catch (final NumberFormatException ex) {
      throw new RuntimeException("Invalid amount string: " + value, ex);
    }
  }
}
