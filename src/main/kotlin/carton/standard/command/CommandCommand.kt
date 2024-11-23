package burrow.carton.standard.command

import burrow.carton.standard.printer.AllFurnishingsCommandsPrinter
import burrow.carton.standard.printer.TopLevelFurnishingsCommandsPrinter
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Option

@CommandLine.Command(
    name = "command",
    description = [
        "Displays available commands for each furnishing in the system."
    ],
)
class CommandCommand(data: CommandData) : Command(data) {
    @Option(
        names = ["--all", "-a"],
        description = [
            "Displays commands for all furnishings, including nested furnishings."
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

        return ExitCode.OK
    }
}