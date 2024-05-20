package me.jameschan.burrow.furniture.builtin.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.config.Config;
import me.jameschan.burrow.furniture.Furniture;
import me.jameschan.burrow.hoard.Entry;

public class TimeFurniture extends Furniture {
  public TimeFurniture(final Chamber chamber) {
    super(chamber);
  }

  @Override
  public Collection<String> configKeys() {
    return List.of(ConfigKey.TIME_DATE_FORMAT);
  }

  @Override
  public void initConfig(final Config config) {
    config.set(ConfigKey.TIME_DATE_FORMAT, "MM/dd, yyyy");
  }

  @Override
  public void toEntryObject(final Map<String, String> entryObject, final Entry entry) {
    entryObject.put(EntryKey.CREATED_AT, entry.get(EntryKey.CREATED_AT));
  }

  @Override
  public void toEntry(final Entry entry, final Map<String, String> entryObject) {
    entry.set(EntryKey.CREATED_AT, entryObject.get(EntryKey.CREATED_AT));
  }

  @Override
  public void toFormattedObject(final Map<String, String> printedObject, final Entry entry) {
    final var createdAt = entry.get(EntryKey.CREATED_AT);
    if (createdAt != null) {
      final var format = getContext().getConfig().get(ConfigKey.TIME_DATE_FORMAT);
      final var createdAtMs = Long.parseLong(createdAt);
      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
      LocalDateTime dateTime =
          LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAtMs), ZoneId.systemDefault());
      printedObject.put(EntryKey.CREATED_AT, dateTime.format(formatter));
    }
  }

  @Override
  public void onCreateEntry(final Entry entry) {
    entry.set(EntryKey.CREATED_AT, String.valueOf(System.currentTimeMillis()));
  }

  public static final class EntryKey {
    public static final String CREATED_AT = "created_at";
  }

  public static final class ConfigKey {
    public static final String TIME_DATE_FORMAT = "time.date_format";
  }
}
