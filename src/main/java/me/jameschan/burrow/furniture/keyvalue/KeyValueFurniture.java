package me.jameschan.burrow.furniture.keyvalue;

import java.util.*;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;
import org.springframework.lang.NonNull;

@BurrowFurniture(
    simpleName = "KeyValue",
    description = "Implemented key-value pair functionalities for entries.")
public class KeyValueFurniture extends Furniture {
  public static final String DEFAULT_KEY_NAME = "key";
  public static final String DEFAULT_VALUE_NAME = "value";

  private final Map<String, Set<Integer>> idSetStore = new HashMap<>();

  public KeyValueFurniture(@NonNull final Chamber chamber) {
    super(chamber);
  }

  @NonNull
  public String getKeyName() {
    return Objects.requireNonNull(
        context.getConfig().getOrDefault(ConfigKey.KV_KEY_NAME, DEFAULT_KEY_NAME));
  }

  @NonNull
  public String getValueName() {
    return Objects.requireNonNull(
        context.getConfig().getOrDefault(ConfigKey.KV_VALUE_NAME, DEFAULT_VALUE_NAME));
  }

  @Override
  public void init() {
    registerCommand(NewCommand.class);
    registerCommand(KeyCommand.class);
    registerCommand(KeyCountCommand.class);
    registerCommand(KeysCommand.class);
    registerCommand(ValuesCommand.class);
  }

  @Override
  public void initConfig(@NonNull final Config config) {
    config.setIfAbsent(ConfigKey.KV_KEY_NAME, DEFAULT_KEY_NAME);
    config.setIfAbsent(ConfigKey.KV_VALUE_NAME, DEFAULT_VALUE_NAME);
  }

  @Override
  public Collection<String> configKeys() {
    return List.of(ConfigKey.KV_KEY_NAME, ConfigKey.KV_VALUE_NAME);
  }

  @NonNull
  public Map<String, Set<Integer>> getIdSetStore() {
    return idSetStore;
  }

  @NonNull
  public Set<Integer> getIdSetByKey(@NonNull final String key) {
    return idSetStore.getOrDefault(key, Set.of());
  }

  @Override
  public void onCreateEntry(@NonNull final Entry entry) {
    final var key = entry.get(getKeyName());
    idSetStore.computeIfAbsent(key, k -> new HashSet<>()).add(entry.getId());
  }

  @Override
  public void onDeleteEntry(@NonNull final Entry entry) {
    final var key = entry.get(getKeyName());
    final var idList = idSetStore.get(key);
    if (idList != null) {
      idList.remove(entry.getId());
      if (idList.isEmpty()) {
        idSetStore.remove(key);
      }
    }
  }

  @Override
  public void toEntry(@NonNull final Entry entry, @NonNull final Map<String, String> entryObject) {
    final var key = entryObject.getOrDefault(getKeyName(), "");
    final var value = entryObject.getOrDefault(getValueName(), "");
    entry.set(getKeyName(), key);
    entry.set(getValueName(), value);

    idSetStore.computeIfAbsent(key, k -> new HashSet<>()).add(entry.getId());
  }

  @Override
  public void toEntryObject(
      @NonNull final Map<String, String> entryObject, @NonNull final Entry entry) {
    entryObject.put(getKeyName(), entry.get(getKeyName()));
    entryObject.put(getValueName(), entry.get(getValueName()));
  }

  @Override
  public void toFormattedObject(
      @NonNull final Map<String, String> printedObject, @NonNull final Entry entry) {
    toEntryObject(printedObject, entry);
  }

  @NonNull
  public static String getKey(
      @NonNull final Entry entry, @NonNull KeyValueFurniture keyValueFurniture) {
    return Objects.requireNonNull(entry.get(keyValueFurniture.getKeyName()));
  }

  @NonNull
  public static String getValue(
      @NonNull final Entry entry, @NonNull KeyValueFurniture keyValueFurniture) {
    return Objects.requireNonNull(entry.get(keyValueFurniture.getValueName()));
  }

  public static void setKey(
      @NonNull final Entry entry,
      @NonNull final String key,
      @NonNull KeyValueFurniture keyValueFurniture) {
    entry.set(keyValueFurniture.getKeyName(), key);
  }

  public static void setValue(
      @NonNull final Entry entry,
      @NonNull final String value,
      @NonNull KeyValueFurniture keyValueFurniture) {
    entry.set(keyValueFurniture.getValueName(), value);
  }

  public static final class ConfigKey {
    public static final String KV_KEY_NAME = "kv.key";
    public static final String KV_VALUE_NAME = "kv.value";
  }
}
