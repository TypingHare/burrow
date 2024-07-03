package burrow.furniture.keyvalue;

import burrow.chain.Context;
import burrow.core.chain.UpdateEntryChain;
import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberContext;
import burrow.core.common.Values;
import burrow.core.config.Config;
import burrow.core.entry.Entry;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.entry.EntryFurniture;
import org.springframework.lang.NonNull;

import java.util.*;

@BurrowFurniture(
    simpleName = "KeyValue",
    description = "Implemented key-value pair functionalities for entries.",
    dependencies = {
        EntryFurniture.class
    }
)
public final class KeyValueFurniture extends Furniture {
    public static final String DEFAULT_KEY_NAME = "key";
    public static final String DEFAULT_VALUE_NAME = "value";
    public static final String COMMAND_TYPE = "KeyValue";

    private final Map<String, Set<Integer>> idSetStore = new HashMap<>();

    public KeyValueFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @NonNull
    public String getKeyName() {
        return context.getConfig().getRequireNotNull(ConfigKey.KV_KEY_NAME);
    }

    @NonNull
    public String getValueName() {
        return context.getConfig().getRequireNotNull(ConfigKey.KV_VALUE_NAME);
    }

    @Override
    public void init() {
        registerCommand(NewCommand.class);
        registerCommand(KeysCommand.class);
        registerCommand(KeyCountCommand.class);
        registerCommand(ValuesCommand.class);
        registerCommand(KeyNameCommand.class);
        registerCommand(ValueNameCommand.class);

        context.getOverseer().getCreateEntryChain().pre.use(this::createEntry);
        context.getOverseer().getRegisterEntryChain().pre.use(this::registerEntry);
        context.getOverseer().getDeleteEntryChain().pre.use(this::deleteEntry);
    }

    @Override
    public void initConfig(@NonNull final Config config) {
        config.setIfAbsent(ConfigKey.KV_KEY_NAME, DEFAULT_KEY_NAME);
        config.setIfAbsent(ConfigKey.KV_VALUE_NAME, DEFAULT_VALUE_NAME);
        config.setIfAbsent(ConfigKey.KV_ALLOW_DUPLICATE, true);
    }

    @Override
    public Collection<String> configKeys() {
        return List.of(ConfigKey.KV_KEY_NAME, ConfigKey.KV_VALUE_NAME, ConfigKey.KV_ALLOW_DUPLICATE);
    }

    @NonNull
    public Map<String, Set<Integer>> getIdSetStore() {
        return idSetStore;
    }

    @NonNull
    public Set<Integer> getIdSetByKey(@NonNull final String key) {
        return idSetStore.getOrDefault(key, Set.of());
    }

    public void createEntry(@NonNull final Context ctx) {
        final var entry = UpdateEntryChain.entryHook.get(ctx);
        final var key = entry.get(getKeyName());
        final var id = entry.getId();
        idSetStore.computeIfAbsent(key, k -> new HashSet<>()).add(id);

        entry.setIfAbsent(getKeyName(), "");
        entry.setIfAbsent(getValueName(), "");
    }

    public void deleteEntry(@NonNull final Context ctx) {
        final var entry = UpdateEntryChain.entryHook.get(ctx);
        final var key = entry.get(getKeyName());
        final var idList = idSetStore.get(key);
        if (idList != null) {
            idList.remove(entry.getId());
            if (idList.isEmpty()) {
                idSetStore.remove(key);
            }
        }
    }

    public void registerEntry(@NonNull final Context ctx) {
        createEntry(ctx);
    }

    @NonNull
    public static String getKey(
        @NonNull final KeyValueFurniture keyValueFurniture,
        @NonNull final Entry entry
    ) {
        return entry.getRequireNonNull(keyValueFurniture.getKeyName());
    }

    @NonNull
    public String getValue(@NonNull final Entry entry) {
        return entry.getRequireNonNull(getValueName());
    }

    public void setKey(
        @NonNull final Entry entry,
        @NonNull final String key
    ) {
        entry.set(getKeyName(), key);
    }

    public void setValue(
        @NonNull final Entry entry,
        @NonNull final String value

    ) {
        entry.set(getValueName(), value);
    }

    public Entry createEntryWithKeyValue(
        @NonNull final String key,
        @NonNull final String value
    ) {
        final var keyValueFurniture = context.getRenovator().getFurniture(KeyValueFurniture.class);
        final var allowDuplicate = keyValueFurniture.isAllowsDuplicate();
        if (!allowDuplicate && keyValueFurniture.idSetStore.containsKey(key)) {
            throw new RuntimeException(
                "Fail to create a new entry because duplicate key is now allowed: " + key);
        }

        final var properties = new HashMap<String, String>();
        properties.put(keyValueFurniture.getKeyName(), key);
        properties.put(keyValueFurniture.getValueName(), value);

        return context.getHoard().create(properties);
    }

    public boolean isAllowsDuplicate() {
        return Values.Bool.parse(context.getConfig()
            .getRequireNotNull(ConfigKey.KV_ALLOW_DUPLICATE));
    }

    @NonNull
    public List<String> getValueListByKey(@NonNull final String key) {
        final var hoard = context.getHoard();
        final var keyValueFurniture = use(KeyValueFurniture.class);
        final var idList = keyValueFurniture.getIdSetByKey(key);
        final var valueName = getValueName();
        return idList.stream()
            .map(hoard::getById)
            .map(entry -> entry.get(valueName))
            .toList();
    }

    public static int countByKey(
        @NonNull final ChamberContext chamberContext,
        @NonNull final String key
    ) {
        return chamberContext
            .getRenovator()
            .getFurniture(KeyValueFurniture.class)
            .getIdSetByKey(key)
            .size();
    }

    public @interface ConfigKey {
        String KV_KEY_NAME = "kv.key";
        String KV_VALUE_NAME = "kv.value";
        String KV_ALLOW_DUPLICATE = "kv.allow_duplicate";
    }
}
