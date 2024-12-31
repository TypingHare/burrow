package burrow.carton.core.command

import burrow.carton.core.Core
import burrow.carton.core.printer.FurnishingClassesPrinter
import burrow.carton.core.printer.FurnishingClassesPrinterContext
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "furnishing",
    description = [
        "Displays a list of furnishings that are installed."
    ]
)
class FurnishingCommand(data: CommandData) : Command(data) {
    @Option(
        names = ["--tree", "-t"],
        description = ["Displays furnishings as a dependency tree."]
    )
    private var shouldDisplayTree = false

    @Option(
        names = ["--all", "-a"],
        description = ["Displays all available furnishings."]
    )
    private var shouldDisplayAll = false

    override fun call(): Int {
        val core = use(Core::class)
        if (shouldDisplayTree) {

        } else {
            if (shouldDisplayAll) {
                val list = core.getAvailableFurnishingClasses()
                val context = FurnishingClassesPrinterContext(list, 80)
                FurnishingClassesPrinter(stdout, context).print()
            } else {
                val list = core.getFurnishingClasses().toList()
                val context = FurnishingClassesPrinterContext(list, 80)
                FurnishingClassesPrinter(stdout, context).print()
            }
        }

        return ExitCode.OK
    }
}