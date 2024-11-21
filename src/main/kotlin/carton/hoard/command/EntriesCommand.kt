package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.printer.EntriesContext
import burrow.carton.hoard.printer.EntriesPrinter
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
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
        val entries = entryIds.map { hoard[it] }.toList()
        EntriesPrinter(stdout, EntriesContext(entries, chamber)).print()

        return ExitCode.OK
    }
}