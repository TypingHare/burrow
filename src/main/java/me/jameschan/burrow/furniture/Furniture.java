package me.jameschan.burrow.furniture;

import java.util.Collection;
import java.util.Map;
import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.chamber.ChamberBased;
import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.config.Config;
import me.jameschan.burrow.hoard.Entry;

public abstract class Furniture extends ChamberBased implements ConfigSupport, EntrySupport {
  public Furniture(final Chamber chamber) {
    super(chamber);
  }

  public void registerCommand(final Class<? extends Command> command) {
    getContext().getCommandManager().register(command);
  }

  public void disableCommand(final Class<? extends Command> command) {
    getContext().getCommandManager().disable(command);
  }

  @Override
  public Collection<String> configKeys() {
    return null;
  }

  @Override
  public void initConfig(final Config config) {}

  @Override
  public void toEntryObject(final Map<String, String> entryObject, final Entry entry) {}

  @Override
  public void toEntry(final Entry entry, final Map<String, String> entryObject) {}

  @Override
  public void onCreateEntry(final Entry entry) {}

  @Override
  public void toFormattedObject(final Map<String, String> printedObject, final Entry entry) {}
}
