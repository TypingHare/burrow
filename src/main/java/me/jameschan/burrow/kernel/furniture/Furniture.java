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
import org.springframework.lang.NonNull;

public abstract class Furniture extends ChamberModule
    implements ConfigSupport, EntrySupport, FormatterSupport {
  private final Set<Class<? extends Command>> commandSet = new HashSet<>();

  public Furniture(@NonNull final Chamber chamber) {
    super(chamber);
  }

  /**
   * Uses a specified furniture. The used furniture should be a dependency of this furniture.
   *
   * @param furnitureClass The class of the furniture to use.
   * @return The furniture object
   * @param <T> The type of the furniture.
   */
  @NonNull
  public <T extends Furniture> T use(final Class<T> furnitureClass) {
    return context.getRenovator().getFurniture(furnitureClass);
  }

  public void registerCommand(@NonNull final Class<? extends Command> commandClass) {
    commandSet.add(commandClass);
    context.getProcessor().register(commandClass);
  }

  @NonNull
  public Collection<Class<? extends Command>> getAllCommands() {
    return commandSet;
  }

  /**
   * Initializes this furniture. This method will be called after all its dependencies are resolved.
   */
  public void init() {}

  public void onTerminate() {}

  @Override
  public Collection<String> configKeys() {
    return null;
  }

  @Override
  public void initConfig(@NonNull final Config config) {}

  @Override
  public void toEntryObject(final Map<String, String> entryObject, final Entry entry) {}

  @Override
  public void toEntry(final Entry entry, final Map<String, String> entryObject) {}

  @Override
  public void onCreateEntry(final Entry entry) {}

  @Override
  public void onUpdateEntry(
      @NonNull final Entry entry, @NonNull final Map<String, String> properties) {}

  @Override
  public void onDeleteEntry(final Entry entry) {}

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
