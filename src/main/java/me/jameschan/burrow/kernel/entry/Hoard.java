package me.jameschan.burrow.kernel.entry;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberModule;
import me.jameschan.burrow.kernel.common.Constants;
import me.jameschan.burrow.kernel.common.Types;
import me.jameschan.burrow.kernel.context.ChamberContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Hoard extends ChamberModule {
  public static final String KEY_ID = "id";

  private final Map<Integer, Entry> entryStore = new HashMap<>();
  private Integer maxId = 0;

  public Hoard(final Chamber chamber) {
    super(chamber);
  }

  public void loadFromFile() {
    final var filePath = context.getRootDir().resolve(Constants.HOARD_FILE_NAME);
    if (!filePath.toFile().exists()) {
      // Create a database file and write "[]"
      try {
        Files.write(filePath, "[]".getBytes());
        return;
      } catch (final IOException ex) {
        throw new RuntimeException("Fail to create database: " + filePath, ex);
      }
    }

    // Update context
    context.set(ChamberContext.Key.HOARD_FILE, filePath.toFile());

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
    final var objects = getAllEntries().stream().map(this::getEntryObject).toList();
    final var json = new Gson().toJson(objects, Types.STRING_STRING_MAP_List);

    try {
      Files.write(hoardFile.toPath(), json.getBytes());
    } catch (final IOException ex) {
      throw new RuntimeException("Fail to save hoard for chamber: " + chamber.getName(), ex);
    }
  }

  public boolean exist(final int id) {
    return entryStore.containsKey(id);
  }

  public Entry getById(final int id) {
    return Optional.ofNullable(entryStore.get(id))
        .orElseThrow(() -> new EntryNotFoundException(id));
  }

  public Entry create(final Map<String, String> properties) {
    final var id = ++maxId;
    final var entry = new Entry(id);

    this.entryStore.put(id, entry);
    properties.forEach(entry::set);

    context.getRenovator().getAllFurniture().forEach(furniture -> furniture.onCreateEntry(entry));

    return entry;
  }

  public void register(final Map<String, String> entryObject) {
    final var id = Integer.parseInt(entryObject.get(KEY_ID));
    if (entryStore.containsKey(id)) {
      throw new DuplicateIdException(id);
    }

    final var entry = new Entry(id);
    entryStore.put(id, entry);
    maxId = Math.max(maxId, id);

    context
        .getRenovator()
        .getAllFurniture()
        .forEach(furniture -> furniture.toEntry(entry, entryObject));
  }

  public Entry delete(final int id) {
    final var entry = getById(id);
    context.getRenovator().getAllFurniture().forEach(furniture -> furniture.onDeleteEntry(entry));
    entryStore.remove(id);

    return entry;
  }

  public Collection<Entry> getAllEntries() {
    return entryStore.values();
  }

  public Map<String, String> getEntryObject(final Entry entry) {
    final var renovator = context.getRenovator();
    final var entryObject = new HashMap<String, String>();
    entryObject.put(KEY_ID, String.valueOf(entry.getId()));
    renovator.getAllFurniture().forEach(furniture -> furniture.toEntryObject(entryObject, entry));

    return entryObject;
  }

  public Map<String, String> getFormattedObject(final Entry entry) {
    final var entryObject = new HashMap<String, String>();
    context
        .getRenovator()
        .getAllFurniture()
        .forEach(furniture -> furniture.toFormattedObject(entryObject, entry));

    return entryObject;
  }

  //  public String getFormattedEntryString(final Entry entry) {
  //    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  //    final var object = getFormattedObject(entry);
  //    final var prettierString = gson.toJson(object).replaceAll("\"(\\w+)\":", "$1:");
  //    return "[" + entry.getId() + "] " + prettierString;
  //  }
}
