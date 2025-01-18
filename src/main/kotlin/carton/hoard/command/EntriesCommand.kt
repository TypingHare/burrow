package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.printer.EntriesPrinter
import burrow.carton.hoard.printer.EntriesPrinterContext
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "entries",
    header = ["Displays specified entries."],
)
class EntriesCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0..*",
        description = ["A list of IDs of entries to retrieve."]
    )
    private var entryIds: Array<Int> = arrayOf()

    override fun call(): Int {
        val storage = use(Hoard::class).storage
        val entries = entryIds
            .map { storage[it] }
            .map { it.id to storage.formatStore(it) }
            .toList()
        EntriesPrinter(stdout, EntriesPrinterContext(entries)).print()

        return ExitCode.OK
    }
}