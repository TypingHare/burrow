package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.printer.EntriesPrinter
import burrow.carton.hoard.printer.EntriesPrinterContext
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "entries",
    description = ["List specified entries."],
)
class EntriesCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0..*",
        description = ["A list of IDs of entries to retrieve."]
    )
    private var entryIds: Array<Int> = arrayOf()

    override fun call(): Int {
        val hoard = use(Hoard::class)
        val entries = entryIds
            .map { hoard[it] }
            .map { it.id to hoard.formatStore(it) }
            .toList()
        EntriesPrinter(stdout, EntriesPrinterContext(entries)).print()

        return ExitCode.OK
    }
}