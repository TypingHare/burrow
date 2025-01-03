package burrow.carton.core.command

import burrow.carton.core.help.CommandParser
import burrow.carton.core.printer.SynopsisPrintContext
import burrow.carton.core.printer.SynopsisPrinter
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "help",
    header = ["Displays the help information of a command."]
)
class HelpCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "command",
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
        val header = burrowCommand.header.let { it.firstOrNull() ?: "" }
        stdout.println(header)
        stdout.println()

        // Usage
        stdout.println("SYNOPSIS")
        val commandParameters = CommandParser.getCommandParameters(commandClass)
        val commandOptions = CommandParser.getCommandOptions(commandClass)
        SynopsisPrinter(
            stdout,
            SynopsisPrintContext(
                "    ${chamber.name} ${burrowCommand.name}",
                commandParameters,
                commandOptions,
                getTerminalSize().width
            )
        ).print()

        // Description
        val descriptionParagraphs = burrowCommand.description.toList()
        if (descriptionParagraphs.isNotEmpty()) {
            stdout.println()
            stdout.println("DESCRIPTION")
            stdout.println(descriptionParagraphs.joinToString("\n"))
        }

        // Parameters
        if (commandParameters.isNotEmpty()) {
            stdout.println()
            stdout.println("PARAMETERS")
            val table = mutableListOf<List<String>>()
            for (parameter in commandParameters) {
                val left = when (parameter.isOptional) {
                    true -> "[<${parameter.label}>]"
                    false -> "<${parameter.label}>"
                }

                table.add(listOf("", left, parameter.description[0]))
            }
            TablePrinter(
                stdout,
                TablePrinterContext(table, getTerminalSize().width).apply {
                    defaultSpacing = 4
                }
            ).print()
        }

        // Options
        if (commandOptions.isNotEmpty()) {
            stdout.println()
            stdout.println("OPTIONS")
            val table = mutableListOf<List<String>>()
            for (option in commandOptions) {
                var left = option.longName ?: ""
                if (option.shortName != null) {
                    if (left.isNotEmpty()) {
                        left += ", " + option.shortName
                    } else {
                        left = option.shortName
                    }
                }
                table.add(listOf("", left, option.description[0]))
            }

            TablePrinter(
                stdout,
                TablePrinterContext(table, getTerminalSize().width).apply {
                    defaultSpacing = 4
                }
            ).print()
        }

        return ExitCode.OK
    }
}