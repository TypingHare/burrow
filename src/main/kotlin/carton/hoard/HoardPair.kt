package burrow.carton.hoard

import burrow.carton.hoard.command.pair.PairCountCommand
import burrow.carton.hoard.command.pair.PairKeysCommand
import burrow.carton.hoard.command.pair.PairNewCommand
import burrow.carton.hoard.command.pair.PairValuesCommand
import burrow.kernel.Burrow
import burrow.kernel.config.Config
import burrow.kernel.converter.StringConverterPairs
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies

@Furniture(
    version = Burrow.VERSION,
    description = "Implemented key-value pair functionalities for entries.",
    type = Furniture.Type.COMPONENT
)
@RequiredDependencies(Dependency(Hoard::class, Burrow.VERSION))
class HoardPair(renovator: Renovator) : Furnishing(renovator) {
    /**
     * Mapping keys to corresponding set of IDs
     */
    val idSetStore = mutableMapOf<Any, MutableSet<Int>>()

    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.KEY_NAME)
        config.addKey(ConfigKey.VALUE_NAME)
        config.addKey(
            ConfigKey.ALLOW_DUPLICATE_KEYS,
            StringConverterPairs.BOOLEAN
        )
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.KEY_NAME, Default.KEY_NAME)
        config.setIfAbsent(ConfigKey.VALUE_NAME, Default.VALUE_NAME)
        config.setIfAbsent(
            ConfigKey.ALLOW_DUPLICATE_KEYS,
            Default.ALLOW_DUPLICATE_KEYS
        )
    }

    override fun assemble() {
        registerCommand(PairNewCommand::class)
        registerCommand(PairCountCommand::class)
        registerCommand(PairKeysCommand::class)
        registerCommand(PairValuesCommand::class)

        courier.subscribe(EntryRestoreEvent::class) {
            addEntry(it.entry)
        }

        courier.subscribe(EntryCreateEvent::class) {
            addEntry(it.entry)
        }

        courier.subscribe(EntryDeleteEvent::class) {
            removeFromIdSetStore(it.entry, getKey<Any>(it.entry))
        }
    }

    private fun getKeyName(): String = config.getNotNull(ConfigKey.KEY_NAME)

    private fun getValueName(): String = config.getNotNull(ConfigKey.VALUE_NAME)

    fun <K> getKey(entry: Entry) = entry.get<K>(getKeyName())!!

    fun <V> getValue(entry: Entry) = entry.get<V>(getValueName())!!

    private fun getAllowDuplication(): Boolean =
        config.getNotNull(ConfigKey.ALLOW_DUPLICATE_KEYS)

    @Throws(DuplicateKeyNotAllowedException::class)
    fun createEntry(key: Any, value: Any): Entry {
        checkKeyDuplication(key)

        return use(Hoard::class).create(
            mutableMapOf(
                getKeyName() to key.toString(),
                getValueName() to value.toString(),
            )
        )
    }

    fun getEntries(key: String): List<Entry> {
        val hoard = use(Hoard::class)
        return idSetStore[key]?.map { hoard[it] } ?: emptyList()
    }

    @Throws(DuplicateKeyNotAllowedException::class)
    private fun checkKeyDuplication(key: Any) {
        if (getAllowDuplication()) return

        if (idSetStore.containsKey(key)) {
            throw DuplicateKeyNotAllowedException(key)
        }
    }

    private fun addToIdSetStore(entry: Entry, key: Any) {
        idSetStore.computeIfAbsent(key) { mutableSetOf() }.add(entry.id)
    }

    private fun removeFromIdSetStore(entry: Entry, key: Any) {
        idSetStore[key]?.let { idSet ->
            idSet.remove(entry.id)
            if (idSet.isEmpty()) idSetStore.remove(key)
        }
    }

    private fun addEntry(entry: Entry) {
        val key = getKey<Any>(entry)
        checkKeyDuplication(key)
        addToIdSetStore(entry, key)
    }

    object ConfigKey {
        const val KEY_NAME = "hoard.pair.key_name"
        const val VALUE_NAME = "hoard.pair.value_name"
        const val ALLOW_DUPLICATE_KEYS = "hoard.pair.allows_duplicate_keys"
    }

    object Default {
        const val KEY_NAME = "key"
        const val VALUE_NAME = "value"
        const val ALLOW_DUPLICATE_KEYS = true
    }
}

class DuplicateKeyNotAllowedException(key: Any) :
    RuntimeException("Duplicate keys are not allowed in this chamber: $key")