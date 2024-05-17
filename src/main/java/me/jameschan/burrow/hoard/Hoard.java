package me.jameschan.burrow.hoard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.chamber.ChamberBased;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Hoard extends ChamberBased {
  public static final String KEY_ID = "id";

  private final Map<Integer, Entry> byId = new HashMap<>();

  private Integer maxId = 0;

  public Hoard(final Chamber chamber) {
    super(chamber);
  }

  public Entry getById(final int id) {
    return Optional.ofNullable(byId.get(id)).orElseThrow(() -> new EntryNotFoundException(id));
  }

  public boolean exist(final int id) {
    return byId.containsKey(id);
  }

  public Entry create(final Map<String, String> properties) {
    final var id = ++maxId;
    final var entry = new Entry(id);

    this.byId.put(id, entry);
    properties.forEach(entry::set);

    return entry;
  }

  public void register(final Map<String, String> entryObject) {
    final var id = Integer.parseInt(entryObject.get(KEY_ID));
    if (byId.containsKey(id)) {
      throw new DuplicateIdException(id);
    }

    final var entry = new Entry(id);
    byId.put(id, entry);
    maxId = Math.max(maxId, id);
  }

  public Entry delete(final int id) {
    final var entry = getById(id);
    byId.remove(id);

    return entry;
  }

  public Map<String, String> getEntryObject(final Entry entry) {
    final var object = new HashMap<String, String>();
    object.put(KEY_ID, String.valueOf(entry.getId()));
    object.putAll(entry.getProperties());

    return object;
  }

  public String getFormattedEntryString(final Entry entry, final boolean removeId) {
    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    final var object = getEntryObject(entry);
    if (removeId) {
      object.remove("id");
    }

    final var prettierString = gson.toJson(object).replaceAll("\"(\\w+)\":", "$1:");
    return "[" + entry.getId() + "] " + prettierString + "\n";
  }
}
