package burrow.carton.hoard.command

import burrow.carton.hoard.EntryNotFoundException
import burrow.carton.hoard.Hoard
import burrow.carton.hoard.printer.EntryPrinter
import burrow.carton.hoard.printer.EntryPrinterContext
import burrow.carton.hoard.printer.EntryPropertiesPrinter
import burrow.carton.hoard.printer.EntryPropertiesPrinterContext
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "entry",
    description = ["Finds an entry by its associated ID and displays it."]
)
class EntryCommand(data: CommandData) : Command(data) {
    @Parameters(index = "0")
    private var id = 0

    @Option(
        names = ["-p", "--properties"],
        description = ["Displays the raw properties."],
        defaultValue = "false"
    )
    private var shouldDisplayProperties = false

    @Throws(EntryNotFoundException::class)
    override fun call(): Int {
        if (id <= 0) {
            stderr.println(
                "Error: Entry ID must be a positive integer. Provided ID: $id"
            )
            return ExitCode.USAGE
        }

        val hoard = use(Hoard::class)
        val entry = hoard[id]
        when (shouldDisplayProperties) {
            true -> EntryPropertiesPrinter(
                stdout,
                EntryPropertiesPrinterContext(entry.toProperties())
            )
            false -> EntryPrinter(
                stdout,
                EntryPrinterContext(entry.id, hoard.formatStore(entry))
            )
        }.print()

        return ExitCode.OK
    }
}