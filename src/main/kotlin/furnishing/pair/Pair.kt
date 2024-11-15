package burrow.furnishing.pair

import burrow.furnishing.hoard.Entry
import burrow.furnishing.hoard.EntryPostCreateEvent
import burrow.furnishing.hoard.EntryPostRegisterEvent
import burrow.furnishing.hoard.Hoard
import burrow.kernel.chamber.Chamber
import burrow.kernel.config.Config
import burrow.kernel.furnishing.DependsOn
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.Furniture


@Furniture(
    version = "0.0.0",
    description = "Implemented key-value pair functionalities for entries.",
    type = Furniture.Type.COMPONENT
)
@DependsOn([Hoard::class])
class Pair(chamber: Chamber) : Furnishing(chamber) {
    private val hoard = use(Hoard::class)

    // Mapping keys to corresponding set of IDs
    private val idSetStore = mutableMapOf<String, MutableSet<Int>>()

    private var keyName: String = Default.KEY_NAME
    private var valueName: String = Default.VALUE_NAME

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
        affairManager.subscribe(EntryPostRegisterEvent::class) {
            add(it.entry)
        }

        affairManager.subscribe(EntryPostCreateEvent::class) {
            add(it.entry)
        }
    }

    override fun launch() {
        keyName = config.get<String>(ConfigKey.KEY_NAME)!!
        valueName = config.get<String>(ConfigKey.KEY_NAME)!!
    }

    private fun add(entry: Entry) {
        val key = entry.get<String>(keyName)!!
        idSetStore.computeIfAbsent(key) { mutableSetOf() }.add(entry.id)
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