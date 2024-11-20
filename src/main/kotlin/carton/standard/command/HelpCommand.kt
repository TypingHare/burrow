package burrow.carton.standard.command

import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode

@CommandLine.Command(
    name = "help",
    description = ["Displays the usage of a specific command."]
)
class HelpCommand(data: CommandData) : Command(data) {
    @CommandLine.Parameters(
        index = "0",
        description = ["The name of the command."],
        defaultValue = ""
    )
    private var commandName: String = ""

    override fun call(): Int {
        return if (commandName.isEmpty())
            displayChamberUsage()
        else
            displayCommandUsage(commandName)
    }

    private fun displayChamberUsage(): Int {
        return ExitCode.OK
    }

    private fun displayCommandUsage(commandName: String): Int {
        val commandClass = processor.commandClasses[commandName]
        if (commandClass == null) {
            stderr.println("Unknown command $commandName")
            return ExitCode.USAGE
        }

        stdout.println(CommandLine(commandClass.java).usageMessage)
        return ExitCode.OK
    }
}