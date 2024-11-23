package burrow.carton.hoard

import burrow.carton.hoard.command.PairCountCommand
import burrow.carton.hoard.command.PairKeysCommand
import burrow.carton.hoard.command.PairNewCommand
import burrow.carton.hoard.command.PairValues
import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.config.Config
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.annotation.DependsOn
import burrow.kernel.furnishing.annotation.Furniture

@Furniture(
    version = Burrow.VERSION.NAME,
    description = "Implemented key-value pair functionalities for entries.",
    type = Furniture.Type.COMPONENT
)
@DependsOn(Hoard::class)
class HoardPair(chamber: Chamber) : Furnishing(chamber) {
    // Mapping keys to corresponding set of IDs
    val idSetStore = mutableMapOf<String, MutableSet<Int>>()

    private var keyName = Default.KEY_NAME
    private var valueName = Default.VALUE_NAME
    private var allowDuplicationKeys = Default.ALLOW_DUPLICATE_KEYS

    override fun prepareConfig(config: Config) {
        config.addKey(
            ConfigKey.ALLOW_DUPLICATE_KEYS,
            { it.toBoolean() },
            { it.toString() }
        )
        config.addKey(ConfigKey.KEY_NAME)
        config.addKey(ConfigKey.VALUE_NAME)
        config.addKey(ConfigKey.ALLOW_DUPLICATE_KEYS, Config.Handler.BOOLEAN)
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
        registerCommand(PairValues::class)

        affairManager.subscribe(EntryRestoreEvent::class) {
            val entry = it.entry
            checkKeyDuplication(entry)
            addToIdSetStore(it.entry)
        }

        affairManager.subscribe(EntryCreateEvent::class) {
            val entry = it.entry
            checkKeyDuplication(entry)
            addToIdSetStore(entry)
        }

        affairManager.subscribe(EntryDeleteEvent::class) {
            removeToIdSetStore(it.entry)
        }
    }

    override fun launch() {
        keyName = config.get<String>(ConfigKey.KEY_NAME)!!
        valueName = config.get<String>(ConfigKey.VALUE_NAME)!!
    }

    fun createEntry(key: String, value: Any): Entry {
        return use(Hoard::class).create(
            mutableMapOf(
                getKeyName() to key,
                getValueName() to value.toString(),
            )
        )
    }

    @Throws(DuplicateKeyNotAllowedException::class)
    private fun checkKeyDuplication(entry: Entry) {
        if (allowDuplicationKeys) return

        val key = getKey(entry)
        if (idSetStore.containsKey(key)) {
            throw DuplicateKeyNotAllowedException(key)
        }
    }

    private fun addToIdSetStore(entry: Entry) {
        val key = getKey(entry)
        idSetStore.computeIfAbsent(key) { mutableSetOf() }.add(entry.id)
    }

    private fun removeToIdSetStore(entry: Entry) {
        val key = entry.getProp(keyName) ?: return
        idSetStore[key]?.let { idSet ->
            idSet.remove(entry.id)
            if (idSet.isEmpty()) idSetStore.remove(key)
        }
    }

    private fun getKeyName(): String = keyName

    private fun getValueName(): String = valueName

    fun getKey(entry: Entry): String = entry.getProp(keyName)!!

    fun getValue(entry: Entry): String = entry.getProp(valueName)!!

    fun getEntries(key: String): List<Entry> {
        val hoard = use(Hoard::class)
        return idSetStore[key]?.map { hoard[it] } ?: emptyList()
    }

    object Default {
        const val KEY_NAME = "key"
        const val VALUE_NAME = "value"
        const val ALLOW_DUPLICATE_KEYS = true
    }

    object ConfigKey {
        const val KEY_NAME = "hoard.pair.key_name"
        const val VALUE_NAME = "hoard.pair.value_name"
        const val ALLOW_DUPLICATE_KEYS = "hoard.pair.allows_duplicate_keys"
    }
}

class DuplicateKeyNotAllowedException(key: String) :
    RuntimeException("Duplicate keys are not allowed in this chamber: $key")