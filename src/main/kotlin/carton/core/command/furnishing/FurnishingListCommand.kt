package burrow.carton.core.command.furnishing

import burrow.carton.core.Core
import burrow.carton.core.printer.FurnishingClassesPrinter
import burrow.carton.core.printer.FurnishingClassesPrinterContext
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "furnishing.list",
    header = [
        "Displays a list of furnishings that are installed."
    ]
)
class FurnishingListCommand(data: CommandData) : Command(data) {
    @Option(
        names = ["--all", "-a"],
        description = ["Displays all available furnishings."]
    )
    private var shouldDisplayAll = false

    override fun call(): Int {
        val core = use(Core::class)
        val maxColumns = getTerminalWidth()
        val furnishingClasses = core.getFurnishingClasses().toList()
        val list = when (shouldDisplayAll) {
            true -> core.getAvailableFurnishingClasses()
            false -> furnishingClasses
        }
        val context = FurnishingClassesPrinterContext(list, maxColumns).apply {
            if (shouldDisplayAll) {
                addStarBeforeInstalledFurnishing = true
                installedFurnishingClasses = furnishingClasses
            }
        }
        FurnishingClassesPrinter(stdout, context).print()

        return ExitCode.OK
    }
}