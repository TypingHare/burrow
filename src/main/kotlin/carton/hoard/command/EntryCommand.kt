package burrow.carton.hoard.command

import burrow.carton.hoard.EntryNotFoundException
import burrow.carton.hoard.Hoard
import burrow.carton.hoard.printer.EntryContext
import burrow.carton.hoard.printer.EntryPropertiesPrinter
import burrow.carton.hoard.printer.EntryStorePrinter
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.*

@CommandLine.Command(
    name = "entry",
    description = ["Finds an entry by its associated ID and displays it."]
)
class EntryCommand(data: CommandData) : Command(data) {
    @Parameters(index = "0")
    private var id = 0

    @Option(
        names = ["-r", "--raw"],
        description = ["Displays the raw properties."],
        defaultValue = "false"
    )
    private var shouldDisplayRawProperties = false

    @Throws(EntryNotFoundException::class)
    override fun call(): Int {
        if (id <= 0) {
            stderr.println(
                "Error: Entry ID must be a positive integer. Provided ID: $id"
            )
            return ExitCode.USAGE
        }

        val entry = use(Hoard::class)[id]
        if (shouldDisplayRawProperties) {
            EntryPropertiesPrinter(stdout, EntryContext(entry, chamber)).print()
        } else {
            EntryStorePrinter(stdout, EntryContext(entry, chamber)).print()
        }

        return ExitCode.OK
    }
}