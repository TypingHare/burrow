package burrow.furniture.pair;

import burrow.chain.Chain;
import burrow.core.chamber.Chamber;
import burrow.core.common.Values;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.hoard.Entry;
import burrow.furniture.hoard.HoardFurniture;
import burrow.furniture.hoard.chain.CreateEntryContext;
import burrow.furniture.hoard.chain.DeleteEntryContext;
import burrow.furniture.hoard.chain.RegisterEntryContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@BurrowFurniture(
    simpleName = "Pair",
    description = "Implemented key-value pair functionalities for entries.",
    type = BurrowFurniture.Type.COMPONENT,
    dependencies = {
        HoardFurniture.class
    }
)
public final class PairFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Pair";

    private final Map<String, Set<Integer>> idSetStore = new HashMap<>();
    private String keyName = EntryKey.KEY;
    private String valueName = EntryKey.VALUE;

    public PairFurniture(@NotNull final Chamber chamber) {
        super(chamber);
    }

    public boolean isAllowsDuplicate() {
        return Values.Bool.parse(getConfig().getNotNull(ConfigKey.PAIR_ALLOW_DUPLICATE));
    }

    @NotNull
    public Map<String, Set<Integer>> getIdSetStore() {
        return idSetStore;
    }

    @Override
    public Collection<String> configKeys() {
        return List.of(
            ConfigKey.PAIR_ALLOW_DUPLICATE,
            ConfigKey.PAIR_KEY_NAME,
            ConfigKey.PAIR_VALUE_NAME
        );
    }

    @Override
    public void initializeConfig(@NotNull final Config config) {
        config.setIfAbsent(ConfigKey.PAIR_ALLOW_DUPLICATE, true);
        config.setIfAbsent(ConfigKey.PAIR_KEY_NAME, EntryKey.KEY);
        config.setIfAbsent(ConfigKey.PAIR_VALUE_NAME, EntryKey.VALUE);
    }

    @Override
    public void beforeInitialization() {
        registerCommand(NewCommand.class);
        registerCommand(KeyListCommand.class);
        registerCommand(KeyCountCommand.class);
        registerCommand(ValueListCommand.class);

        keyName = getConfig().getNotNull(ConfigKey.PAIR_KEY_NAME);
        valueName = getConfig().getNotNull(ConfigKey.PAIR_VALUE_NAME);
    }

    @Override
    public void initialize() {
        final var hoardFurniture = use(HoardFurniture.class);
        final var hoard = hoardFurniture.getHoard();
        hoard.getCreateEntryChain().use(this::createEntry);
        hoard.getRegisterEntryChain().use(this::registerEntry);
        hoard.getDeleteEntryChain().use(this::deleteEntry);
    }

    @NotNull
    public Set<Integer> getIdSetByKey(@NotNull final String key) {
        return idSetStore.getOrDefault(key, Set.of());
    }

    public void createEntry(
        @NotNull final CreateEntryContext context,
        @Nullable final Runnable next
    ) {
        final var entry = context.getEntry();
        final var key = entry.get(keyName);
        final var id = entry.getId();
        idSetStore.computeIfAbsent(key, k -> new HashSet<>()).add(id);

        Chain.runIfNotNull(next);
    }

    public void deleteEntry(
        @NotNull final DeleteEntryContext context,
        @Nullable final Runnable next
    ) {
        final var entry = context.getEntry();
        final var key = entry.get(keyName);
        final var idSet = idSetStore.get(key);
        if (idSet != null) {
            idSet.remove(entry.getId());
            if (idSet.isEmpty()) {
                idSetStore.remove(key);
            }
        }

        Chain.runIfNotNull(next);
    }

    public void registerEntry(
        @NotNull final RegisterEntryContext context,
        @Nullable final Runnable next
    ) {
        final var entry = context.getEntry();
        final var key = entry.get(keyName);
        final var idSet = idSetStore.computeIfAbsent(key, k -> new HashSet<>());
        idSet.add(entry.getId());

        Chain.runIfNotNull(next);
    }

    @NotNull
    public String getKey(@NotNull final Entry entry) {
        return entry.getNotNull(keyName);
    }

    @NotNull
    public String getValue(@NotNull final Entry entry) {
        return entry.getNotNull(valueName);
    }

    public void setKey(@NotNull final Entry entry, @NotNull final String key) {
        entry.set(keyName, key);
    }

    public void setValue(@NotNull final Entry entry, @NotNull final String value) {
        entry.set(valueName, value);
    }

    @NotNull
    public Entry createEntryWithKeyValue(
        @NotNull final String key,
        @NotNull final String value
    ) {
        final var allowDuplicate = isAllowsDuplicate();
        if (!allowDuplicate && idSetStore.containsKey(key)) {
            throw new RuntimeException(
                "Fail to create a new entry because duplicate key is now allowed: " + key);
        }

        final var properties = new HashMap<String, String>();
        properties.put(keyName, key);
        properties.put(valueName, value);

        return use(HoardFurniture.class).getHoard().create(properties);
    }

    @NotNull
    public List<String> getValueListByKey(@NotNull final String key) {
        final var hoard = use(HoardFurniture.class).getHoard();
        final var idList = getIdSetByKey(key);
        return idList.stream()
            .map(hoard::get)
            .map(entry -> entry.get(valueName))
            .toList();
    }

    public int countByKey(@NotNull final String key) {
        return getIdSetByKey(key).size();
    }

    public @interface ConfigKey {
        String PAIR_ALLOW_DUPLICATE = "pair.allow_duplicate";
        String PAIR_KEY_NAME = "pair.key_name";
        String PAIR_VALUE_NAME = "pair.value_name";
    }

    public @interface EntryKey {
        String KEY = "key";
        String VALUE = "value";
    }
}
