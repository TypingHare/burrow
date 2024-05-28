package me.jameschan.burrow.kernel.furniture.support;

import java.util.Map;
import me.jameschan.burrow.kernel.entry.Entry;

public interface EntrySupport {
  void toEntryObject(final Map<String, String> entryObject, final Entry entry);

  void toEntry(final Entry entry, final Map<String, String> entryObject);

  void onCreateEntry(final Entry entry);

  void toFormattedObject(final Map<String, String> printedObject, final Entry entry);
}
