package burrow.core.entry;

import burrow.core.chain.UpdateEntryChain;
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

    public Hoard(@NonNull final Chamber chamber) {
        super(chamber);

        final var overseer = chamber.getContext().getOverseer();
        overseer.getCreateEntryChain().pre.use((ctx) -> {
            final var entry = UpdateEntryChain.entryHook.get(ctx);
            final var properties = UpdateEntryChain.propertiesHook.get(ctx);
            properties.forEach(entry::set);
        });
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

    /**
     * Save all entries to a hoard file.
     */
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

        final var createEntryChain = context.getOverseer().getCreateEntryChain();
        final var context = createEntryChain.createContext(entry, properties);
        createEntryChain.apply(context);

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

    public void register(@NonNull final Map<String, String> entryObject) {
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

        final var registerEntryChain = context.getOverseer().getRegisterEntryChain();
        final var context = registerEntryChain.createContext(entry, entryObject);
        registerEntryChain.apply(context);

        ++size;
    }

    @NonNull
    public Entry setProperties(final int id, @NonNull final Map<String, String> properties)
        throws EntryNotFoundException {
        final var entry = getById(id);
        entry.getProperties().putAll(properties);

        final var updateEntryChain = context.getOverseer().getSetEntryChain();
        final var context = updateEntryChain.createContext(entry, properties);
        updateEntryChain.apply(context);

        return entry;
    }

    @NonNull
    public Entry unsetProperties(final int id, @NonNull final Collection<String> keys)
        throws EntryNotFoundException {
        final var entry = getById(id);
        final var properties = entry.getProperties();
        keys.forEach(properties::remove);

        final var unsetEntryChain = context.getOverseer().getUnsetEntryChain();
        final var context = unsetEntryChain.createContext(entry, keys);
        unsetEntryChain.apply(context);

        return entry;
    }

    @NonNull
    public Entry delete(final int id) throws EntryNotFoundException {
        final var entry = getById(id);
        final var deleteEntryChain = context.getOverseer().getDeleteEntryChain();
        final var context = deleteEntryChain.createContext(entry, Map.of());
        deleteEntryChain.apply(context);

        // Remove the entry from the store
        entryStore.set(id, null);

        return entry;
    }

    @NonNull
    public List<Entry> getAllEntries() {
        return entryStore.stream().filter(Objects::nonNull).toList();
    }

    @NonNull
    public Map<String, String> getEntryObject(@NonNull final Entry entry) {
        final var entryObject = new HashMap<>(entry.getProperties());
        entryObject.put(KEY_ID, String.valueOf(entry.getId()));
        final var toEntryObjectChain = context.getOverseer().getToEntryObjectChain();
        final var context = toEntryObjectChain.createContext(entry, entryObject);
        toEntryObjectChain.apply(context);

        return entryObject;
    }

    public int getSize() {
        return size;
    }

    public boolean exist(final int id) {
        return id > 0 && id < entryStore.size() && entryStore.get(id) != null;
    }
}
