package me.jameschan.burrow.furniture.keyvalue;

import java.util.*;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;
import org.springframework.lang.NonNull;

@BurrowFurniture(
    simpleName = "KeyValue",
    description = "Implemented key-value pair functionalities for entries.")
public class KeyValueFurniture extends Furniture {
  private final Map<String, Set<Integer>> idSetStore = new HashMap<>();

  public KeyValueFurniture(final Chamber chamber) {
    super(chamber);
  }

  @Override
  public void init() {
    registerCommand(NewCommand.class);
    registerCommand(KeyCommand.class);
    registerCommand(KeyCountCommand.class);
    registerCommand(KeysCommand.class);
    registerCommand(ValuesCommand.class);
  }

  public Map<String, Set<Integer>> getIdSetStore() {
    return idSetStore;
  }

  public Set<Integer> getIdSetByKey(final String key) {
    return idSetStore.getOrDefault(key, Set.of());
  }

  @Override
  public void onCreateEntry(final Entry entry) {
    final var key = entry.get(EntryKey.KEY);
    idSetStore.computeIfAbsent(key, k -> new HashSet<>()).add(entry.getId());
  }

  @Override
  public void onDeleteEntry(final Entry entry) {
    final var key = entry.get(EntryKey.KEY);
    final var idList = idSetStore.get(key);
    if (idList != null) {
      idList.remove(entry.getId());
      if (idList.isEmpty()) {
        idSetStore.remove(key);
      }
    }
  }

  @Override
  public void toEntry(final Entry entry, final Map<String, String> entryObject) {
    final var key = entryObject.get(EntryKey.KEY);
    final var value = entryObject.get(EntryKey.VALUE);
    entry.set(EntryKey.KEY, key);
    entry.set(EntryKey.VALUE, value);

    idSetStore.computeIfAbsent(key, k -> new HashSet<>()).add(entry.getId());
  }

  @Override
  public void toEntryObject(final Map<String, String> entryObject, final Entry entry) {
    entryObject.put(EntryKey.KEY, entry.get(EntryKey.KEY));
    entryObject.put(EntryKey.VALUE, entry.get(EntryKey.VALUE));
  }

  @Override
  public void toFormattedObject(final Map<String, String> printedObject, final Entry entry) {
    toEntryObject(printedObject, entry);
  }

  @NonNull
  public static String getKey(@NonNull final Entry entry) {
    return Objects.requireNonNull(entry.get(EntryKey.KEY));
  }

  @NonNull
  public static String getValue(@NonNull final Entry entry) {
    return Objects.requireNonNull(entry.get(EntryKey.VALUE));
  }

  public static final class EntryKey {
    public static final String KEY = "key";
    public static final String VALUE = "value";
  }
}
