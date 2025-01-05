package burrow.carton.haystack.command.opener

import burrow.carton.haystack.Haystack
import burrow.carton.haystack.HaystackOpener
import burrow.kernel.terminal.*
import burrow.carton.haystack.command.InfoCommand as HaystackInfoCommand

@BurrowCommand(
    name = "info",
    header = ["Displays the information of a relative path."]
)
class InfoCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The relative path."],
    )
    private var relativePath = ""

    override fun call(): Int {
        val exitCode =
            dispatch(HaystackInfoCommand::class, listOf(relativePath))
        if (exitCode != ExitCode.OK) {
            return exitCode
        }

        val entry = use(Haystack::class).getEntry(relativePath)
        val opener = entry.get<String>(HaystackOpener.EntryKey.OPENER)
        stdout.println("opener: $opener")

        return ExitCode.OK
    }
}