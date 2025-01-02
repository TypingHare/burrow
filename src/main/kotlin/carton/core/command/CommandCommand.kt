package burrow.carton.core.command

import burrow.carton.core.Core
import burrow.carton.core.printer.FurnishingCommandClassesPrinter
import burrow.carton.core.printer.FurnishingCommandClassesPrinterContext
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "command",
    description = [
        "Displays available commands for each furnishing in the system."
    ]
)
class CommandCommand(data: CommandData) : Command(data) {
    @Option(
        names = ["--all", "-a"],
        description = [
            "Displays commands for all furnishings, including nested furnishings."
        ],
        defaultValue = "false"
    )
    private var shouldDisplayAll = false

    override fun call(): Int {
        val core = use(Core::class)
        val furnishingCommandClasses = when (shouldDisplayAll) {
            true -> core.getFurnishingCommandClasses(onlyTopLevel = false)
            false -> core.getFurnishingCommandClasses(onlyTopLevel = true)
        }

        FurnishingCommandClassesPrinter(
            stdout,
            FurnishingCommandClassesPrinterContext(
                furnishingCommandClasses,
                getTerminalSize().width
            )
        ).print()

        return ExitCode.OK
    }
}