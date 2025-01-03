package burrow.carton.dictator.command

import burrow.carton.dictator.Dictator
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "chamber.list",
    header = ["Displays the list of all built chambers."]
)
class ChamberListCommand(data: CommandData) : Command(data) {
    @Option(
        names = ["-b", "--blueprint"],
        description = ["Checks all the blueprint instead of built chambers."],
        defaultValue = "false"
    )
    var shouldShowBlueprints = false

    override fun call(): Int {
        val dictator = use(Dictator::class)
        val chamberInfoList = when (shouldShowBlueprints) {
            true -> dictator.getAvailableChamberInfoList()
            false -> dictator.getBuiltChamberInfoList()
        }

        val table = mutableListOf<List<String>>().apply {
            chamberInfoList.forEach {
                add(listOf(it.name, it.description))
            }
        }

        TablePrinter(
            stdout,
            TablePrinterContext(table, getTerminalWidth())
        ).print()

        return ExitCode.OK
    }
}