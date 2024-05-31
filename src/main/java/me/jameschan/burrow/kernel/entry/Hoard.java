package me.jameschan.burrow.kernel.entry;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberModule;
import me.jameschan.burrow.kernel.common.Types;
import me.jameschan.burrow.kernel.context.ChamberContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Hoard extends ChamberModule {
  public static final String HOARD_FILE_NAME = "hoard.json";
  public static final String KEY_ID = "id";

  private final List<Entry> entryStore = new ArrayList<>();
  private Integer maxId = 0;

  public Hoard(final Chamber chamber) {
    super(chamber);
  }

  public void loadFromFile() {
    final var filePath = context.getRootDir().resolve(HOARD_FILE_NAME);
    context.set(ChamberContext.Key.HOARD_FILE, filePath.toFile());
    if (!filePath.toFile().exists()) {
      // Create a database file and write "[]"
      try {
        Files.write(filePath, "[]".getBytes());
        return;
      } catch (final IOException ex) {
        throw new RuntimeException("Fail to create database: " + filePath, ex);
      }
    }

    try {
      final var content = Files.readString(filePath);
      final List<Map<String, String>> entries =
          new Gson().fromJson(content, Types.STRING_STRING_MAP_List);
      entries.forEach(this::register);
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void saveToFile() {
    final var hoardFile = context.getHoardFile();
    final var objectList =
        getAllEntries().stream().filter(Objects::nonNull).map(this::getEntryObject).toList();
    final var json = new Gson().toJson(objectList, Types.STRING_STRING_MAP_List);

    try {
      Files.write(hoardFile.toPath(), json.getBytes());
    } catch (final IOException ex) {
      throw new RuntimeException("Fail to save hoard for chamber: " + chamber.getName(), ex);
    }
  }

  @NonNull
  public Entry getById(final int id) throws EntryNotFoundException {
    try {
      final var entry = entryStore.get(id);
      if (entry == null) {
        throw new EntryNotFoundException(id);
      }

      return entry;
    } catch (final IndexOutOfBoundsException ex) {
      throw new EntryNotFoundException(id);
    }
  }

  @NonNull
  public Entry create(final Map<String, String> properties) {
    final int id = ++maxId;
    final var entry = new Entry(id);
    properties.forEach(entry::set);
    context.getRenovator().getAllFurniture().forEach(furniture -> furniture.onCreateEntry(entry));

    // Put the entry to the store
    for (int i = entryStore.size(); i <= id; ++i) entryStore.add(null);
    this.entryStore.set(id, entry);

    return entry;
  }

  public void register(final Map<String, String> entryObject) {
    final var id = Integer.parseInt(entryObject.get(KEY_ID));
    try {
      getById(id);
      throw new DuplicateIdException(id);
    } catch (final EntryNotFoundException ignored) {
    }

    final var entry = new Entry(id);
    entryObject.forEach(entry::set);

    for (int i = entryStore.size(); i <= id; ++i) entryStore.add(null);
    entryStore.set(id, entry);
    maxId = Math.max(maxId, id);

    context
        .getRenovator()
        .getAllFurniture()
        .forEach(furniture -> furniture.toEntry(entry, entryObject));
  }

  @NonNull
  public Entry update(final int id, final Map<String, String> properties)
      throws EntryNotFoundException {
    final var entry = getById(id);
    entry.getProperties().putAll(properties);

    context
        .getRenovator()
        .getAllFurniture()
        .forEach(furniture -> furniture.onUpdateEntry(entry, properties));

    return entry;
  }

  @NonNull
  public Entry delete(final int id) throws EntryNotFoundException {
    final var entry = getById(id);
    context.getRenovator().getAllFurniture().forEach(furniture -> furniture.onDeleteEntry(entry));
    entryStore.remove(id);

    return entry;
  }

  @NonNull
  public Collection<Entry> getAllEntries() {
    return entryStore.stream().filter(Objects::nonNull).toList();
  }

  @NonNull
  public Map<String, String> getEntryObject(final Entry entry) {
    final var renovator = context.getRenovator();
    final var entryObject = new HashMap<>(entry.getProperties());
    entryObject.put(KEY_ID, String.valueOf(entry.getId()));
    renovator.getAllFurniture().forEach(furniture -> furniture.toEntryObject(entryObject, entry));

    return entryObject;
  }
}
