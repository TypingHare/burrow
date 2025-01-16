package burrow.carton.core.command

import burrow.carton.core.Core
import burrow.kernel.terminal.*

@BurrowCommand(
    name = Core.NOT_FOUND_COMMAND_NAME,
    header = [
        "This command is executed when the command is not found."
    ]
)
class NotFoundCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The command name."]
    )
    private var commandName = ""

    override fun call(): Int {
        stderr.println("Command not found: $commandName")

        return ExitCode.OK
    }
}