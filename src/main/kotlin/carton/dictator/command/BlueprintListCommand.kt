package burrow.carton.dictator.command

import burrow.carton.dictator.Dictator
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "blueprint.list",
    description = ["Displays the list of all chamber blueprints."]
)
class BlueprintListCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val dictator = use(Dictator::class)
        val table = dictator.getBlueprintInfoList().map {
            listOf(it.name, it.description)
        }

        val context = TablePrinterContext(table, getTerminalWidth())
        TablePrinter(stdout, context).print()
        
        return ExitCode.OK
    }
}