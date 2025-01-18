package burrow.carton.core.command

import burrow.carton.core.Core
import burrow.carton.core.printer.FurnishingCommandClassesPrinter
import burrow.carton.core.printer.FurnishingCommandClassesPrinterContext
import burrow.kernel.furniture.Furnishing
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "command",
    header = [
        "Displays available commands for each furnishing in the system."
    ]
)
class CommandCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = [
            "Only displays the commands of a specified furnishing ID."
        ],
        defaultValue = "",
    )
    private var furnishingName = ""

    @Option(
        names = ["--all", "-a"],
        description = [
            "Displays commands for all furnishings, including nested furnishings."
        ],
        defaultValue = "false"
    )
    private var shouldDisplayAll = false

    override fun call(): Int {
        return when (furnishingName.isBlank()) {
            true -> displayMultipleFurnishingCommands()
            false -> displaySingleFurnishingCommands()
        }
    }

    private fun displayMultipleFurnishingCommands(): Int {
        val core = use(Core::class)
        val furnishingCommandClasses =
            core.getFurnishingCommandClasses(onlyTopLevel = !shouldDisplayAll)
        printFurnishingCommands(furnishingCommandClasses)

        return ExitCode.OK
    }

    private fun displaySingleFurnishingCommands(): Int {
        val furnishingId = renovator.getUniqueFurnishingId(furnishingName)
        val furnishing = renovator.getFurnishing(furnishingId)!!
        printFurnishingCommands(
            mapOf(furnishing to furnishing.commandClasses.toList())
        )

        return ExitCode.OK
    }

    private fun printFurnishingCommands(
        furnishingCommandClasses: Map<Furnishing, List<CommandClass>>
    ) {
        FurnishingCommandClassesPrinter(
            stdout,
            FurnishingCommandClassesPrinterContext(
                furnishingCommandClasses,
                getTerminalWidth()
            )
        ).print()
    }
}