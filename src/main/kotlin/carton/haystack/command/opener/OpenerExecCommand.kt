package burrow.carton.haystack.command.opener

import burrow.carton.cradle.command.ExecCommand
import burrow.carton.haystack.Haystack
import burrow.carton.haystack.HaystackOpener
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Parameters

@BurrowCommand(
    name = "opener.exec",
    description = [
        "Opens a relative path using the opener associated with the entry."
    ]
)
class OpenerExecCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "<relative-path>",
        description = ["The relative path of the entry."]
    )
    private var relativePath = ""

    override fun call(): Int {
        val entry = use(Haystack::class).getEntry(relativePath)
        val opener = HaystackOpener.extractOpener(entry)
        val absolutePath = Haystack.extractAbsolutePath(entry)
        val command = "$opener $absolutePath"

        return dispatch(ExecCommand::class, listOf(command))
    }
}