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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;

@BurrowFurniture(
    simpleName = "Pair",
    description = "Implemented key-value pair functionalities for entries.",
    dependencies = {
        HoardFurniture.class
    }
)
public final class PairFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Pair";

    private final Map<String, Set<Integer>> idSetStore = new HashMap<>();

    public PairFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    public boolean isAllowsDuplicate() {
        return Values.Bool.parse(getConfig().getNonNull(ConfigKey.PAIR_ALLOW_DUPLICATE));
    }

    @NonNull
    public Map<String, Set<Integer>> getIdSetStore() {
        return idSetStore;
    }

    @Override
    public Collection<String> configKeys() {
        return List.of(ConfigKey.PAIR_ALLOW_DUPLICATE);
    }

    @Override
    public void initializeConfig(@NonNull final Config config) {
        config.setIfAbsent(ConfigKey.PAIR_ALLOW_DUPLICATE, true);
    }

    @Override
    public void beforeInitialization() {
        registerCommand(NewCommand.class);
        registerCommand(KeysCommand.class);
        registerCommand(KeyCountCommand.class);
        registerCommand(ValuesCommand.class);
    }

    @Override
    public void initialize() {
        final var hoardFurniture = use(HoardFurniture.class);
        hoardFurniture.getHoard().getCreateEntryChain().use(this::createEntry);
        hoardFurniture.getHoard().getRegisterEntryChain().use(this::registerEntry);
        hoardFurniture.getHoard().getDeleteEntryChain().use(this::deleteEntry);
    }

    @NonNull
    public Set<Integer> getIdSetByKey(@NonNull final String key) {
        return idSetStore.getOrDefault(key, Set.of());
    }

    public void createEntry(
        @NonNull final CreateEntryContext context,
        @Nullable final Runnable next
    ) {
        final var entry = CreateEntryContext.Hook.entry.getNonNull(context);
        final var key = entry.get(EntryKey.KEY);
        final var id = entry.getId();
        idSetStore.computeIfAbsent(key, k -> new HashSet<>()).add(id);

        Chain.runIfNotNull(next);
    }

    public void deleteEntry(
        @NonNull final DeleteEntryContext context,
        @Nullable final Runnable next
    ) {
        final var entry = DeleteEntryContext.Hook.entry.getNonNull(context);
        final var key = entry.get(EntryKey.KEY);
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
        @NonNull final RegisterEntryContext context,
        @Nullable final Runnable next
    ) {
        final var entry = RegisterEntryContext.Hook.entry.getNonNull(context);
        final var key = entry.get(EntryKey.KEY);
        final var idSet = idSetStore.computeIfAbsent(key, k -> new HashSet<>());
        idSet.add(entry.getId());

        Chain.runIfNotNull(next);
    }

    @NonNull
    public String getKey(@NonNull final Entry entry) {
        return entry.getNonNull(EntryKey.KEY);
    }

    @NonNull
    public String getValue(@NonNull final Entry entry) {
        return entry.getNonNull(EntryKey.VALUE);
    }

    public void setKey(@NonNull final Entry entry, @NonNull final String key) {
        entry.set(EntryKey.KEY, key);
    }

    public void setValue(@NonNull final Entry entry, @NonNull final String value) {
        entry.set(EntryKey.VALUE, value);
    }

    @NonNull
    public Entry createEntryWithKeyValue(
        @NonNull final String key,
        @NonNull final String value
    ) {
        final var allowDuplicate = isAllowsDuplicate();
        if (!allowDuplicate && idSetStore.containsKey(key)) {
            throw new RuntimeException(
                "Fail to create a new entry because duplicate key is now allowed: " + key);
        }

        final var properties = new HashMap<String, String>();
        properties.put(EntryKey.KEY, key);
        properties.put(EntryKey.VALUE, value);

        final var hoardFurniture = use(HoardFurniture.class);
        return hoardFurniture.getHoard().create(properties);
    }

    @NonNull
    public List<String> getValueListByKey(@NonNull final String key) {
        final var hoard = use(HoardFurniture.class).getHoard();
        final var keyValueFurniture = use(PairFurniture.class);
        final var idList = keyValueFurniture.getIdSetByKey(key);
        return idList.stream()
            .map(hoard::get)
            .map(entry -> entry.get(EntryKey.VALUE))
            .toList();
    }

    public int countByKey(@NonNull final String key) {
        return getIdSetByKey(key).size();
    }

    public @interface ConfigKey {
        String PAIR_ALLOW_DUPLICATE = "pair.allow_duplicate";
    }

    public @interface EntryKey {
        String KEY = "key";
        String VALUE = "value";
    }
}
