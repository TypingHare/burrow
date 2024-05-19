package me.jameschan.burrow.furniture;

import me.jameschan.burrow.hoard.Entry;

import java.util.Map;

public interface EntrySupport {
  void toEntryObject(final Map<String, String> entryObject, final Entry entry);

  void toEntry(final Entry entry, final Map<String, String> entryObject);

  void onCreateEntry(final Entry entry);

  void toFormattedObject(final Map<String, String> printedObject, final Entry entry);
}
