package burrow.carton.haystack

import burrow.carton.cradle.Cradle
import burrow.carton.haystack.command.opener.InfoCommand
import burrow.carton.haystack.command.opener.OpenCommand
import burrow.carton.haystack.command.opener.OpenerSetCommand
import burrow.carton.hoard.Entry
import burrow.carton.hoard.EntryCreateEvent
import burrow.carton.hoard.EntryRestoreEvent
import burrow.kernel.Burrow
import burrow.kernel.config.Config
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies

@Furniture(
    version = Burrow.VERSION,
    description = "Allows users to set up the opener.",
    type = Furniture.Type.COMPONENT
)
@RequiredDependencies(
    Dependency(Haystack::class, Burrow.VERSION),
    Dependency(Cradle::class, Burrow.VERSION)
)
class HaystackOpener(renovator: Renovator) : Furnishing(renovator) {
    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.DEFAULT)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.DEFAULT, "open")
    }

    override fun assemble() {
        registerCommand(OpenerSetCommand::class)
        registerCommand(OpenCommand::class)
        registerCommand(InfoCommand::class)

        val defaultOpener = getDefaultOpener()
        courier.subscribe(EntryCreateEvent::class) {
            it.entry[EntryKey.OPENER] = defaultOpener
        }

        courier.subscribe(EntryRestoreEvent::class) {
            it.entry.setIfAbsent(EntryKey.OPENER, defaultOpener)
        }
    }

    private fun getDefaultOpener(): String =
        config.getNotNull(ConfigKey.DEFAULT)

    companion object {
        fun extractOpener(entry: Entry): String =
            entry.get<String>(EntryKey.OPENER)
                ?: throw OpenerNotFoundException(entry.id)
    }

    object ConfigKey {
        const val DEFAULT = "opener.default"
    }

    object EntryKey {
        const val OPENER = "opener"
    }
}

class OpenerNotFoundException(entryId: Int) :
    RuntimeException("Opener property not found in entry: $entryId")