package burrow.carton.hoard

import burrow.carton.hoard.command.PairNewCommand
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
@DependsOn([Hoard::class])
class HoardPair(chamber: Chamber) : Furnishing(chamber) {
    // Mapping keys to corresponding set of IDs
    private val idSetStore = mutableMapOf<String, MutableSet<Int>>()

    private var keyName = Default.KEY_NAME
    private var valueName = Default.VALUE_NAME

    override fun prepareConfig(config: Config) {
        config.addKey(
            ConfigKey.ALLOW_DUPLICATE_KEYS,
            { it.toBoolean() },
            { it.toString() }
        )
        config.addKey(ConfigKey.KEY_NAME)
        config.addKey(ConfigKey.VALUE_NAME)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.KEY_NAME, Default.KEY_NAME)
        config.setIfAbsent(ConfigKey.VALUE_NAME, Default.VALUE_NAME)
    }

    override fun assemble() {
        registerCommand(PairNewCommand::class)

        affairManager.subscribe(EntryRestoreEvent::class) {
            addToIdSetStore(it.entry)
        }

        affairManager.subscribe(EntryCreateEvent::class) {
            addToIdSetStore(it.entry)
        }

        affairManager.subscribe(EntryDeleteEvent::class) {
            removeToIdSetStore(it.entry)
        }
    }

    override fun launch() {
        keyName = config.get<String>(ConfigKey.KEY_NAME)!!
        valueName = config.get<String>(ConfigKey.VALUE_NAME)!!
    }

    private fun addToIdSetStore(entry: Entry) {

        val key = entry.getProp(keyName)!!
        idSetStore.computeIfAbsent(key) { mutableSetOf() }.add(entry.id)
    }

    private fun removeToIdSetStore(entry: Entry) {
        val key = entry.getProp(keyName)!!
        idSetStore[key]?.remove(entry.id)
    }

    fun getKeyName(): String = keyName

    fun getValueName(): String = valueName

    object Default {
        const val KEY_NAME = "key"
        const val VALUE_NAME = "value"
    }

    object ConfigKey {
        const val ALLOW_DUPLICATE_KEYS = "pair.allows_duplicate_keys"
        const val KEY_NAME = "pair.key_name"
        const val VALUE_NAME = "pair.value_name"
    }
}