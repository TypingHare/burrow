package me.jameschan.burrow.furniture.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.common.Values;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;
import org.springframework.lang.NonNull;

@BurrowFurniture(
    simpleName = "time",
    description = "Add creation time and modification time of each entry. ")
public class TimeFurniture extends Furniture {
  public static final String DEFAULT_TIME_FORMAT = "MM/dd, yyyy";

  public TimeFurniture(final Chamber chamber) {
    super(chamber);
  }

  @Override
  public Collection<String> configKeys() {
    return List.of(
        ConfigKey.TIME_DATE_FORMAT,
        ConfigKey.TIME_CREATED_AT_ENABLED,
        ConfigKey.TIME_UPDATED_AT_ENABLED);
  }

  @Override
  public void initConfig(@NonNull final Config config) {
    config.setIfAbsent(ConfigKey.TIME_DATE_FORMAT, DEFAULT_TIME_FORMAT);
    config.setIfAbsent(ConfigKey.TIME_CREATED_AT_ENABLED, true);
    config.setIfAbsent(ConfigKey.TIME_UPDATED_AT_ENABLED, true);
  }

  @Override
  public void toEntryObject(final Map<String, String> entryObject, final Entry entry) {
    entryObject.put(EntryKey.CREATED_AT, entry.get(EntryKey.CREATED_AT));
    entryObject.put(EntryKey.UPDATED_AT, entry.get(EntryKey.UPDATED_AT));
  }

  @Override
  public void toEntry(final Entry entry, final Map<String, String> entryObject) {
    entry.set(EntryKey.CREATED_AT, entryObject.getOrDefault(EntryKey.CREATED_AT, ""));
    entry.set(EntryKey.UPDATED_AT, entryObject.getOrDefault(EntryKey.UPDATED_AT, ""));
  }

  @Override
  public void toFormattedObject(final Map<String, String> printedObject, final Entry entry) {
    final Consumer<String> addTime =
        (final String key) -> {
          final var value = entry.get(key);
          if (!value.isEmpty()) {
            final var ms = Long.parseLong(value);
            printedObject.put(key, dateToString(context, ms));
          }
        };

    addTime.accept(EntryKey.CREATED_AT);
    addTime.accept(EntryKey.UPDATED_AT);
  }

  @Override
  public void onCreateEntry(final Entry entry) {
    final var currentTimeMs = System.currentTimeMillis();
    if (Values.Bool.isTrue(context.getConfig().get(ConfigKey.TIME_CREATED_AT_ENABLED))) {
      entry.set(EntryKey.CREATED_AT, currentTimeMs);
    }

    updateEntry(context, entry);
  }

  @Override
  public void onUpdateEntry(
      @NonNull final Entry entry, @NonNull final Map<String, String> properties) {
    updateEntry(context, entry);
  }

  @NonNull
  public static String dateToString(
      @NonNull final ChamberContext chamberContext, final long timestampInMs) {
    final var format =
        chamberContext.getConfig().getOrDefault(ConfigKey.TIME_DATE_FORMAT, DEFAULT_TIME_FORMAT);
    assert format != null;
    final var formatter = DateTimeFormatter.ofPattern(format);
    final var dateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampInMs), ZoneId.systemDefault());
    return dateTime.format(formatter);
  }

  public static void updateEntry(
      @NonNull final ChamberContext chamberContext, @NonNull final Entry entry) {
    final var currentTimeMs = System.currentTimeMillis();
    if (Values.Bool.isTrue(chamberContext.getConfig().get(ConfigKey.TIME_UPDATED_AT_ENABLED))) {
      entry.set(EntryKey.UPDATED_AT, currentTimeMs);
    }
  }

  public static final class EntryKey {
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
  }

  public static final class ConfigKey {
    public static final String TIME_DATE_FORMAT = "time.date_format";
    public static final String TIME_CREATED_AT_ENABLED = "time.created_at.enabled";
    public static final String TIME_UPDATED_AT_ENABLED = "time.updated_at.enabled";
  }
}
