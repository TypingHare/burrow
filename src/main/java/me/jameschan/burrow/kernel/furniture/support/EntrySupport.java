package me.jameschan.burrow.kernel.furniture.support;

import java.util.Map;
import me.jameschan.burrow.kernel.entry.Entry;
import org.springframework.lang.NonNull;

public interface EntrySupport {
  void toEntryObject(final Map<String, String> entryObject, final Entry entry);

  void toEntry(final Entry entry, final Map<String, String> entryObject);

  void onCreateEntry(final Entry entry);

  void onUpdateEntry(@NonNull final Entry entry, @NonNull final Map<String, String> properties);

  void onDeleteEntry(final Entry entry);

  void toFormattedObject(final Map<String, String> formattedObject, final Entry entry);
}
