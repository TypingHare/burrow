package me.jameschan.burrow.kernel.furniture;

import java.util.Collection;
import java.util.Map;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberModule;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.furniture.support.ConfigSupport;
import me.jameschan.burrow.kernel.furniture.support.EntrySupport;

public abstract class Furniture extends ChamberModule implements ConfigSupport, EntrySupport {
  public Furniture(final Chamber chamber) {
    super(chamber);
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
