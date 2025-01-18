package burrow.carton.core.command.furnishing

import burrow.carton.core.Core
import burrow.carton.core.printer.FurnishingClassesPrinter
import burrow.carton.core.printer.FurnishingClassesPrinterContext
import burrow.carton.core.printer.FurnishingClassesTreePrinter
import burrow.carton.core.printer.FurnishingClassesTreePrinterContext
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "furnishing",
    header = [
        "Displays a list of furnishings that are installed."
    ]
)
class FurnishingCommand(data: CommandData) : Command(data) {
    @Option(
        names = ["--all", "-a"],
        description = ["Displays all available furnishings."]
    )
    private var shouldDisplayAll = false

    @Option(
        names = ["--tree", "-t"],
        description = ["Displays a tree of furnishings that are installed."]
    )
    private var shouldDisplayTree = false

    override fun call(): Int {
        return when (shouldDisplayTree) {
            true -> displayTree()
            false -> displayList()
        }
    }

    private fun displayList(): Int {
        val core = use(Core::class)
        val maxColumns = getTerminalWidth()
        val furnishingClasses = core.getFurnishingClasses().toList()
        val list = when (shouldDisplayAll) {
            true -> renovator.getAvailableFurnishingClasses()
                .sortedBy { it.java.name }
            false -> furnishingClasses.sortedBy { it.java.name }
        }.toList()
        val context = FurnishingClassesPrinterContext(list, maxColumns).apply {
            if (shouldDisplayAll) {
                addStarBeforeInstalledFurnishing = true
                installedFurnishingClasses = furnishingClasses
            }
        }
        FurnishingClassesPrinter(stdout, context).print()

        return ExitCode.OK
    }

    private fun displayTree(): Int {
        val core = use(Core::class)
        val maxColumns = getTerminalWidth()
        val tree = core.getFurnishingClassesTree()
        val context = FurnishingClassesTreePrinterContext(tree, maxColumns)
        FurnishingClassesTreePrinter(stdout, context).print()

        return ExitCode.OK
    }
}