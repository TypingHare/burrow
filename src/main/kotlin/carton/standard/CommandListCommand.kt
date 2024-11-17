package burrow.carton.standard

import burrow.carton.standard.printer.AllFurnishingsCommandsPrinter
import burrow.carton.standard.printer.TopLevelFurnishingsCommandsPrinter
import burrow.kernel.command.Command
import burrow.kernel.command.CommandClass
import burrow.kernel.command.CommandData
import burrow.kernel.furnishing.Furnishing
import picocli.CommandLine

@CommandLine.Command(
    name = "command.list",
    description = [
        "Displays available commands for each furnishing in the system."
    ],
)
class CommandListCommand(data: CommandData) : Command(data) {
    @CommandLine.Option(
        names = ["-a", "--all"],
        description = [
            "Displays commands for all furnishings, including nested " +
                    "furnishings."
        ],
        defaultValue = "false"
    )
    var shouldDisplayAll = false

    override fun call(): Int {
        if (shouldDisplayAll) {
            AllFurnishingsCommandsPrinter(stdout, chamber).print()
        } else {
            TopLevelFurnishingsCommandsPrinter(stdout, chamber).print()
        }

        return CommandLine.ExitCode.OK
    }
}

typealias FurnishingsCommandClasses = Map<Furnishing, List<CommandClass>>