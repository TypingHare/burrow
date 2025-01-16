package burrow.carton.haystack.command.opener

import burrow.carton.cradle.command.ExecCommand
import burrow.carton.haystack.Haystack
import burrow.carton.haystack.HaystackOpener
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Parameters

@BurrowCommand(
    name = "open",
    header = [
        "Opens an entry using the opener associated with the entry."
    ]
)
class OpenCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The name of the entry to open."]
    )
    private var name = ""

    override fun call(): Int {
        val entry = use(Haystack::class).getEntry(name)
        val opener = HaystackOpener.extractOpener(entry)
        val absolutePath = Haystack.extractAbsolutePath(entry)
        val command = "$opener $absolutePath"

        return dispatch(ExecCommand::class, listOf(command))
    }
}