package me.jameschan.burrow.furniture.budget;

import me.jameschan.burrow.furniture.keyvalue.KeyValueFurniture;
import me.jameschan.burrow.furniture.keyvalue.KeysCommand;
import me.jameschan.burrow.furniture.time.TimeFurniture;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.command.Processor;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;
import org.springframework.lang.NonNull;

@BurrowFurniture(
    simpleName = "budget",
    dependencies = {KeyValueFurniture.class, TimeFurniture.class})
public class BudgetFurniture extends Furniture {
  public BudgetFurniture(final Chamber chamber) {
    super(chamber);
  }

  @Override
  public void init() {
    registerCommand(NewCommand.class);
    registerCommand(CollectCommand.class);
    registerCommand(SumCommand.class);

    context.getProcessor().setAlias(Processor.getCommandName(KeysCommand.class), "categories");
  }

  @Override
  public void initConfig(@NonNull final Config config) {
    // Disable the updated_at
    config.set(TimeFurniture.ConfigKey.TIME_UPDATED_AT_ENABLED, false);

    // Set the key name and value name
    config.set(KeyValueFurniture.ConfigKey.KV_KEY_NAME, "category");
    config.set(KeyValueFurniture.ConfigKey.KV_VALUE_NAME, "amount");
  }

  @Override
  public void onCreateEntry(final Entry entry) {
    // The value has to be a valid floating point number
    final var keyValueFurniture = use(KeyValueFurniture.class);
    final var value = KeyValueFurniture.getValue(entry, keyValueFurniture);
    try {
      final var amount = Float.parseFloat(value);
      final var amountString = String.format("%.2f", amount);
      KeyValueFurniture.setValue(entry, amountString, keyValueFurniture);
    } catch (final NumberFormatException ex) {
      throw new RuntimeException("Invalid amount string: " + value, ex);
    }
  }

  public static final class EntryKey {
    public static final String DESCRIPTION = "description";
  }
}
