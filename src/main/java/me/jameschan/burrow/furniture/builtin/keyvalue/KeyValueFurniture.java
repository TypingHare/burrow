package me.jameschan.burrow.furniture.builtin.keyvalue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.furniture.Furniture;
import me.jameschan.burrow.furniture.builtin.keyvalue.command.KeyCommand;
import me.jameschan.burrow.furniture.builtin.keyvalue.command.NewCommand;
import me.jameschan.burrow.hoard.Entry;

public class KeyValueFurniture extends Furniture {
  private final Map<String, List<Integer>> byKey = new HashMap<>();

  public KeyValueFurniture(final Chamber chamber) {
    super(chamber);

    registerCommand(KeyCommand.class);
    registerCommand(NewCommand.class);
  }

  public static final class EntryKey {
    public static final String KEY = "key";
    public static final String VALUE = "value";
  }

  @Override
  public void toEntry(final Entry entry, final Map<String, String> entryObject) {
    final var key = entryObject.get(EntryKey.KEY);
    final var value = entryObject.get(EntryKey.VALUE);
    entry.set(EntryKey.KEY, key);
    entry.set(EntryKey.VALUE, value);

    byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(entry.getId());
  }

  @Override
  public void toEntryObject(Map<String, String> entryObject, Entry entry) {
    entryObject.put(EntryKey.KEY, entry.get(EntryKey.KEY));
    entryObject.put(EntryKey.VALUE, entry.get(EntryKey.VALUE));
  }

  public List<Integer> getIdListByKey(final String key) {
    return byKey.getOrDefault(key, List.of());
  }
}
