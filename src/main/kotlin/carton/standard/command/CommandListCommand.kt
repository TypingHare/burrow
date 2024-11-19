package burrow.carton.standard.command

import burrow.carton.standard.printer.AllFurnishingsCommandsPrinter
import burrow.carton.standard.printer.TopLevelFurnishingsCommandsPrinter
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
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