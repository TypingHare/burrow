package me.jameschan.burrow.furniture.keyvalue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;

@BurrowFurniture
public class KeyValueFurniture extends Furniture {
  private final Map<String, List<Integer>> idListStore = new HashMap<>();

  public KeyValueFurniture(final Chamber chamber) {
    super(chamber);

    registerCommand(NewCommand.class);
    registerCommand(KeyCommand.class);
    registerCommand(CountCommand.class);
    registerCommand(KeysCommand.class);
    registerCommand(ValuesCommand.class);
  }

  public Map<String, List<Integer>> getIdListStore() {
    return idListStore;
  }

  public List<Integer> getIdListByKey(final String key) {
    return idListStore.getOrDefault(key, List.of());
  }

  @Override
  public void onCreateEntry(final Entry entry) {
    final var key = entry.get(EntryKey.KEY);
    idListStore.computeIfAbsent(key, k -> new ArrayList<>()).add(entry.getId());
  }

  @Override
  public void onDeleteEntry(final Entry entry) {
    final var key = entry.get(EntryKey.KEY);
    final var idList = idListStore.get(key);
    if (idList != null) {
      idList.remove(entry.getId());
      if (idList.isEmpty()) {
        idListStore.remove(key);
      }
    }
  }

  @Override
  public void toEntry(final Entry entry, final Map<String, String> entryObject) {
    final var key = entryObject.get(EntryKey.KEY);
    final var value = entryObject.get(EntryKey.VALUE);
    entry.set(EntryKey.KEY, key);
    entry.set(EntryKey.VALUE, value);

    idListStore.computeIfAbsent(key, k -> new ArrayList<>()).add(entry.getId());
  }

  @Override
  public void toEntryObject(Map<String, String> entryObject, Entry entry) {
    entryObject.put(EntryKey.KEY, entry.get(EntryKey.KEY));
    entryObject.put(EntryKey.VALUE, entry.get(EntryKey.VALUE));
  }

  @Override
  public void toFormattedObject(Map<String, String> printedObject, Entry entry) {
    toEntryObject(printedObject, entry);
  }

  public static final class EntryKey {
    public static final String KEY = "key";
    public static final String VALUE = "value";
  }
}
