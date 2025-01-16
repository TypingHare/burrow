package burrow.carton.haystack.command.opener

import burrow.carton.haystack.Haystack
import burrow.carton.haystack.HaystackOpener
import burrow.kernel.terminal.*
import burrow.carton.haystack.command.InfoCommand as HaystackInfoCommand

@BurrowCommand(
    name = "info",
    header = ["Displays the information of an entry."]
)
class InfoCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The name of the entry."],
    )
    private var entry = ""

    override fun call(): Int {
        val exitCode =
            dispatch(HaystackInfoCommand::class, listOf(entry))
        if (exitCode != ExitCode.OK) {
            return exitCode
        }

        val entry = use(Haystack::class).getEntry(entry)
        val opener = HaystackOpener.extractOpener(entry)
        stdout.println("opener: $opener")

        return ExitCode.OK
    }
}