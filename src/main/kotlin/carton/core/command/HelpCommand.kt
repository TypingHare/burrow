package burrow.carton.core.command

import burrow.kernel.terminal.*
import picocli.CommandLine
import java.io.PrintWriter
import java.io.StringWriter

@BurrowCommand(
    name = "help",
    header = ["Displays the help information of a command."]
)
class HelpCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "<command>",
        description = ["The name of the command."],
        defaultValue = ""
    )
    private var commandName = ""

    @Option(
        names = ["--json", "-j"],
        description = ["Whether to display the data in JSON form."],
        defaultValue = "false"
    )
    private var useJson = false

    override fun call(): Int {
        return when (commandName.isBlank()) {
            true -> displayAllCommandsInfo()
            false -> displayCommandInfo()
        }
    }

    private fun displayAllCommandsInfo(): Int {
        return ExitCode.OK
    }

    private fun displayCommandInfo(): Int {
        val commandClass = interpreter.commandClasses[commandName]
        if (commandClass == null) {
            stderr.println("Command not found: $commandName")
            return ExitCode.USAGE
        }

        val burrowCommand = extractBurrowCommand(commandClass)

        // Header
        val name = burrowCommand.name
        val header = burrowCommand.header.let { it.firstOrNull() ?: "" }
        stdout.println("$name - $header")
        stdout.println()

        // Usage
        val usage = StringWriter()
            .apply { CommandLine(commandClass).usage(PrintWriter(this)) }
            .toString()
        stdout.println(usage)
        stdout.println()

        val descriptionParagraphs = burrowCommand.description.toList()

        return ExitCode.OK
    }
}