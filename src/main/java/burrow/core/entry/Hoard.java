package burrow.core.entry;

import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberContext;
import burrow.core.chamber.ChamberModule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Hoard extends ChamberModule {
    public static final String HOARD_FILE_NAME = "hoard.json";
    public static final String KEY_ID = "id";

    private List<Entry> entryStore = new ArrayList<>();
    private Integer maxId = 0;
    private Integer size = 0;

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
                new Gson().fromJson(content, new TypeToken<List<Map<String, String>>>() {
                }.getType());
            entries.forEach(this::register);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Clears all entries.
     */
    public void clearAll() {
        this.entryStore = new ArrayList<>();
        maxId = 0;
        size = 0;
    }

    public void saveToFile() {
        final var hoardFile = context.getHoardFile();
        final var objectList =
            getAllEntries().stream().filter(Objects::nonNull).map(this::getEntryObject).toList();
        final var json = new Gson().toJson(objectList, new TypeToken<List<Map<String, String>>>() {
        }.getType());

        try {
            Files.write(hoardFile.toPath(), json.getBytes());
        } catch (final IOException ex) {
            final var chamberName = chamber.getContext().getChamberName();
            throw new RuntimeException("Fail to save hoard for chamber: " + chamberName, ex);
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
    public Entry create(@NonNull final Map<String, String> properties) {
        final int id = ++maxId;
        final var entry = new Entry(id);
        properties.forEach(entry::set);
//        context.getRenovator().getAllFurniture().forEach(furniture -> furniture.onCreateEntry(entry));

        // Put the entry to the store
        for (int i = entryStore.size(); i <= id; ++i) entryStore.add(null);
        this.entryStore.set(id, entry);

        ++size;

        return entry;
    }

    @NonNull
    public Entry create() {
        return create(Map.of());
    }

    public void register(final Map<String, String> entryObject) {
        final var id = Integer.parseInt(entryObject.get(KEY_ID));
        try {
            getById(id);
            throw new DuplicateIdException(id);
        } catch (final EntryNotFoundException ignored) {
        }

        final var entry = new Entry(id);
        entryObject.remove(KEY_ID);
        entryObject.forEach(entry::set);

        for (int i = entryStore.size(); i <= id; ++i) entryStore.add(null);
        entryStore.set(id, entry);
        maxId = Math.max(maxId, id);

//        context
//            .getRenovator()
//            .getAllFurniture()
//            .forEach(furniture -> furniture.toEntry(entry, entryObject));

        ++size;
    }

    @NonNull
    public Entry setProperties(final int id, @NonNull final Map<String, String> properties)
        throws EntryNotFoundException {
        final var entry = getById(id);
        entry.getProperties().putAll(properties);

//        context
//            .getRenovator()
//            .getAllFurniture()
//            .forEach(furniture -> furniture.onUpdateEntry(entry, properties));

        return entry;
    }

    public Entry unsetProperties(final int id, final Collection<String> keys)
        throws EntryNotFoundException {
        final var entry = getById(id);
        final var properties = entry.getProperties();
        keys.forEach(properties::remove);

        return entry;
    }

    @NonNull
    public Entry delete(final int id) throws EntryNotFoundException {
        final var entry = getById(id);
//        context.getRenovator().getAllFurniture().forEach(furniture -> furniture.onDeleteEntry(entry));
        entryStore.set(id, null);

        System.out.println(entryStore.size());
        return entry;
    }

    @NonNull
    public List<Entry> getAllEntries() {
        return entryStore.stream().filter(Objects::nonNull).toList();
    }

    @NonNull
    public Map<String, String> getEntryObject(final Entry entry) {
        final var entryObject = new HashMap<>(entry.getProperties());
        entryObject.put(KEY_ID, String.valueOf(entry.getId()));
//        context.getRenovator().getAllFurniture().forEach(furniture -> furniture.toEntryObject(entryObject, entry));

        return entryObject;
    }

    public Integer getSize() {
        return size;
    }
}
