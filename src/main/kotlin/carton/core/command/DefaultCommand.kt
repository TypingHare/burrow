package burrow.carton.core.command

import burrow.kernel.furniture.extractDescription
import burrow.kernel.terminal.*
import picocli.CommandLine

@BurrowCommand(
    name = Interpreter.DEFAULT_COMMAND_NAME,
    description = [
        "The default command that is executed when no command is specified."
    ]
)
class DefaultCommand(data: CommandData) : Command(data) {
    @CommandLine.Option(
        names = ["--help", "-h"],
        description = ["Displays the help information."],
    )
    private var showHelp = false

    override fun call(): Int {
        if (showHelp) {
            stdout.println(chamber.name + "  help:")
        }

        return ExitCode.OK
    }
}