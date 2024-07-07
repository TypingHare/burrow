package burrow.furniture.hoard;

import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberContext;
import burrow.core.chamber.ChamberModule;
import burrow.core.common.Environment;
import burrow.furniture.hoard.chain.*;
import burrow.furniture.hoard.exception.DuplicateIdException;
import burrow.furniture.hoard.exception.EntryNotFoundException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Hoard extends ChamberModule {
    public static final String HOARD_FILE_NAME = "hoard.json";
    public static final String KEY_ID = "id";

    private final Path hoardFilePath;
    private List<Entry> entryStore = new ArrayList<>();
    private Integer maxId = 0;
    private Integer size = 0;

    private final CreateEntryChain createEntryChain = new CreateEntryChain();
    private final RegisterEntryChain registerEntryChain = new RegisterEntryChain();
    private final DeleteEntryChain deleteEntryChain = new DeleteEntryChain();
    private final SetEntryChain setEntryChain = new SetEntryChain();
    private final UnsetEntryChain unsetEntryChain = new UnsetEntryChain();
    private final ToEntryObjectChain toEntryObjectChain = new ToEntryObjectChain();
    private final ToFormattedObjectChain toFormattedObjectChain = new ToFormattedObjectChain();
    private final FormattedObjectToStringChain formattedObjectToStringChain =
        new FormattedObjectToStringChain();

    public Hoard(@NonNull final Chamber chamber) {
        super(chamber);

        final var rootPath = ChamberContext.Hook.rootPath.getNonNull(getChamberContext());
        hoardFilePath = rootPath.resolve(HOARD_FILE_NAME);
    }

    @NonNull
    public Path getHoardFilePath() {
        return hoardFilePath;
    }

    public int getSize() {
        return size;
    }

    @NonNull
    public CreateEntryChain getCreateEntryChain() {
        return createEntryChain;
    }

    @NonNull
    public RegisterEntryChain getRegisterEntryChain() {
        return registerEntryChain;
    }

    @NonNull
    public DeleteEntryChain getDeleteEntryChain() {
        return deleteEntryChain;
    }

    @NonNull
    public SetEntryChain getSetEntryChain() {
        return setEntryChain;
    }

    @NonNull
    public UnsetEntryChain getUnsetEntryChain() {
        return unsetEntryChain;
    }

    @NonNull
    public ToEntryObjectChain getToEntryObjectChain() {
        return toEntryObjectChain;
    }

    @NonNull
    public ToFormattedObjectChain getToFormattedObjectChain() {
        return toFormattedObjectChain;
    }

    @NonNull
    public FormattedObjectToStringChain getFormattedObjectToStringChain() {
        return formattedObjectToStringChain;
    }

    public boolean exist(final int id) {
        return id > 0 && id < entryStore.size() && entryStore.get(id) != null;
    }

    @NonNull
    public List<Entry> getAllEntries() {
        return entryStore.stream().filter(Objects::nonNull).toList();
    }

    @NonNull
    public Map<String, String> getEntryObject(@NonNull final Entry entry) {
        final var entryObject = new HashMap<>(entry.getProperties());
        entryObject.put(KEY_ID, String.valueOf(entry.getId()));
        toEntryObjectChain.apply(entry, entryObject);

        return entryObject;
    }

    public void loadFromFile(@NonNull final Path hoardFilePath) {
        if (!hoardFilePath.toFile().exists()) {
            // Create a database file and write "[]"
            try {
                Files.write(hoardFilePath, "[]".getBytes());
                return;
            } catch (final IOException ex) {
                throw new RuntimeException("Fail to create hoard: " + hoardFilePath, ex);
            }
        }

        try {
            final var content = Files.readString(hoardFilePath);
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
    public void saveToFile(@NonNull final Path hoardFilePath) {
        final var objectList = getAllEntries().stream()
            .filter(Objects::nonNull)
            .map(this::getEntryObject).toList();
        final var json = new Gson().toJson(objectList, new TypeToken<List<Map<String, String>>>() {
        }.getType());

        try {
            Files.write(hoardFilePath, json.getBytes());
        } catch (final IOException ex) {
            throw new RuntimeException("Fail to save hoard to file." + ex);
        }
    }

    @NonNull
    public Entry create(@NonNull final Map<String, String> properties) {
        final int id = ++maxId;
        final var entry = new Entry(id);
        properties.forEach(entry::set);
        createEntryChain.apply(entry, properties);

        // Put the entry to the store
        for (int i = entryStore.size(); i <= id; ++i) entryStore.add(null);
        this.entryStore.set(id, entry);

        ++size;

        return entry;
    }

    public void register(@NonNull final Map<String, String> entryObject) {
        final var id = Integer.parseInt(entryObject.get(KEY_ID));
        try {
            get(id);
            throw new DuplicateIdException(id);
        } catch (final EntryNotFoundException ignored) {
        }

        final var entry = new Entry(id);
        entryObject.remove(KEY_ID);
        entryObject.forEach(entry::set);

        for (int i = entryStore.size(); i <= id; ++i) entryStore.add(null);
        entryStore.set(id, entry);
        maxId = Math.max(maxId, id);

        entryObject.forEach(entry::set);
        registerEntryChain.apply(entry, entryObject);

        ++size;
    }

    @NonNull
    public Entry get(final int id) throws EntryNotFoundException {
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
    public Entry delete(final int id) throws EntryNotFoundException {
        final var entry = get(id);
        deleteEntryChain.apply(entry);

        // Remove the entry from the store
        entryStore.set(id, null);

        --size;

        return entry;
    }

    @NonNull
    public Entry setProperties(final int id, @NonNull final Map<String, String> properties)
        throws EntryNotFoundException {
        final var entry = get(id);
        entry.getProperties().putAll(properties);

        setEntryChain.apply(entry, properties);

        return entry;
    }

    @NonNull
    public Entry unsetProperties(final int id, @NonNull final Collection<String> keys)
        throws EntryNotFoundException {
        final var entry = get(id);
        final var properties = entry.getProperties();
        keys.forEach(properties::remove);

        final var context = unsetEntryChain.apply(entry, keys);
        unsetEntryChain.apply(context);

        return entry;
    }

    @NonNull
    public String format(
        final int id,
        @NonNull final Map<String, String> formattedObject,
        @NonNull final Environment environment
    ) {
        final var context =
            formattedObjectToStringChain.apply(id, formattedObject, environment);

        return Optional.ofNullable(context.getResult()).orElse("");
    }

    @NonNull
    public String entryToString(
        @NonNull final Entry entry,
        @NonNull final Environment environment
    ) {
        final var context = toFormattedObjectChain.apply(entry);
        final var formattedObject = context.getFormattedObject();
        return format(entry.getId(), formattedObject, environment);
    }
}
