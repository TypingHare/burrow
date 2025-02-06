package burrow.carton.launcher.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.HoardPair
import burrow.carton.launcher.Launcher
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "launcher.set",
    header = ["Sets the launcher for an entry."]
)
class LauncherSetCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "<id>|<name>",
        description = ["The ID or name of the entry."]
    )
    private var idOrName = ""

    @Parameters(
        index = "1",
        description = ["The launcher to set."]
    )
    private var launcher = ""

    private val hoard = use(Hoard::class)

    private val hoardPair = use(HoardPair::class)

    override fun call(): Int {
        val entry = if (idOrName.toIntOrNull() == null)
            hoard.storage[idOrName.toInt()] else
            hoardPair.getFirstEntryOrThrow(idOrName)
        entry[Launcher.EntryKey.LAUNCHER] = launcher

        return ExitCode.OK
    }
}