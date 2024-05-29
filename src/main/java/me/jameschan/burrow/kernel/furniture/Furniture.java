package me.jameschan.burrow.kernel.furniture;

import java.util.*;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberModule;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.furniture.support.ConfigSupport;
import me.jameschan.burrow.kernel.furniture.support.EntrySupport;
import me.jameschan.burrow.kernel.furniture.support.FormatterSupport;

public abstract class Furniture extends ChamberModule
    implements ConfigSupport, EntrySupport, FormatterSupport {
  private final Set<Class<? extends Command>> commandSet = new HashSet<>();

  public Furniture(final Chamber chamber) {
    super(chamber);
  }

  public void registerCommand(final Class<? extends Command> commandClass) {
    commandSet.add(commandClass);
    context.getProcessor().register(commandClass);
  }

  public Collection<Class<? extends Command>> getAllCommands() {
    return commandSet;
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
  public void toFormattedObject(final Map<String, String> formattedObject, final Entry entry) {}

  @Override
  public String formatStringList(final List<String> itemList) {
    return null;
  }

  @Override
  public String formatEntry(final Entry entry, final Map<String, String> formattedObject) {
    return null;
  }
}
