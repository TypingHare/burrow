package burrow.carton.standard.command

import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine

@CommandLine.Command(
    name = "help",
    description = ["Displays the usage of a specific command."]
)
class HelpCommand(data: CommandData) : Command(data) {
    @CommandLine.Parameters(
        index = "0",
        description = ["The name of the command."]
    )
    var commandName: String = ""

    override fun call(): Int {
        stdout.println(commandName)

        return CommandLine.ExitCode.OK
    }
}