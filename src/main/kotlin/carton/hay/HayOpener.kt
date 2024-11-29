package burrow.carton.hay

import burrow.carton.cradle.Cradle
import burrow.carton.hay.command.OpenerExecCommand
import burrow.carton.hay.command.OpenerSetCommand
import burrow.carton.hoard.Entry
import burrow.carton.hoard.EntryCreateEvent
import burrow.carton.hoard.EntryRestoreEvent
import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.config.Config
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.annotation.DependsOn
import burrow.kernel.furnishing.annotation.Furniture

@Furniture(
    version = Burrow.VERSION.NAME,
    description = "Allows users to set up the opener.",
    type = Furniture.Type.COMPONENT
)
@DependsOn(Hay::class, Cradle::class)
class HayOpener(chamber: Chamber) : Furnishing(chamber) {
    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.DEFAULT)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.DEFAULT, "open")
    }

    override fun assemble() {
        registerCommand(OpenerSetCommand::class)
        registerCommand(OpenerExecCommand::class)

        val defaultOpener = getDefaultOpener()
        affairManager.subscribe(EntryCreateEvent::class) {
            it.entry[EntryKey.OPENER] = defaultOpener
        }

        affairManager.subscribe(EntryRestoreEvent::class) {
            it.entry.setIfAbsent(EntryKey.OPENER, defaultOpener)
            it.entry.setPropIfAbsent(EntryKey.OPENER, defaultOpener)
        }
    }

    private fun getDefaultOpener(): String =
        config.getNotNull(ConfigKey.DEFAULT)

    companion object {
        fun extractOpener(entry: Entry): String =
            entry.get<String>(EntryKey.OPENER)!!
    }

    object ConfigKey {
        const val DEFAULT = "opener.default"
    }

    object EntryKey {
        const val OPENER = "opener"
    }
}