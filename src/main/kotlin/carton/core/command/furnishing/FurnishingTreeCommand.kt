package burrow.carton.core.command.furnishing

import burrow.carton.core.Core
import burrow.carton.core.printer.FurnishingClassesTreePrinter
import burrow.carton.core.printer.FurnishingClassesTreePrinterContext
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "furnishing.tree",
    header = [
        "Displays a tree of furnishings that are installed."
    ]
)
class FurnishingTreeCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val core = use(Core::class)
        val maxColumns = getTerminalSize().width
        val tree = core.getFurnishingClassesTree()
        val context = FurnishingClassesTreePrinterContext(tree, maxColumns)
        FurnishingClassesTreePrinter(stdout, context).print()

        return ExitCode.OK
    }
}