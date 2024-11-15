package burrow.carton.standard

import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine

@CommandLine.Command(
    name = "root",
    description = [
        "Displays the absolute path to the root directory of the current " +
                "chamber."
    ]
)
class RootCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        stdout.println(chamber.rootPath)

        return CommandLine.ExitCode.OK
    }
}