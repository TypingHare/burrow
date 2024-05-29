package me.jameschan.burrow.kernel.furniture.support;

import java.util.List;
import java.util.Map;
import me.jameschan.burrow.kernel.entry.Entry;
import org.springframework.lang.Nullable;

public interface FormatterSupport {
  @Nullable
  String formatStringList(final List<String> itemList);

  @Nullable
  String formatEntry(final Entry entry, final Map<String, String> formattedObject);
}
