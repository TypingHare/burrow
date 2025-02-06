package burrow.carton.launcher.command

import burrow.carton.cradle.command.ExecCommand
import burrow.carton.hoard.Hoard
import burrow.carton.hoard.HoardPair
import burrow.carton.launcher.extractLauncher
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Parameters

@BurrowCommand(
    name = "launch",
    header = [
        "Opens an entry using the opener associated with the entry."
    ]
)
class LaunchCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "<id>|<name>",
        description = ["The name of the entry to launch."]
    )
    private var idOrName = ""

    private val hoard = use(Hoard::class)

    private val hoardPair = use(HoardPair::class)

    override fun call(): Int {
        val entry = if (idOrName.toIntOrNull() == null)
            hoard.storage[idOrName.toInt()] else
            hoardPair.getFirstEntryOrThrow(idOrName)
        val value = hoardPair.getValue<String>(entry)
        val launcher = extractLauncher(entry)
        val shellCommand = "$launcher $value"

        return dispatch(ExecCommand::class, listOf(shellCommand))
    }
}