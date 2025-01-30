package burrow.carton.core.command.notfounddispatcher

import burrow.carton.core.Core
import burrow.carton.core.NotFoundDispatcher
import burrow.kernel.terminal.*

@BurrowCommand(
    name = Core.NOT_FOUND_COMMAND_NAME,
    header = [
        "This command is executed when the command is not found; dispatches it to a command."
    ]
)
class NotFoundCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The command name that is not found."]
    )
    private var commandName = ""

    override fun call(): Int {
        stderr.println("Command not found: $commandName")

        val commandToDispatch =
            config.getNotNull<String>(NotFoundDispatcher.ConfigKey.NOT_FOUND_DISPATCHER_COMMAND)
        if (commandToDispatch.isBlank()) {
            return ExitCode.USAGE
        }

        stderr.println("Dispatching to $commandToDispatch with: $commandName")
        val commandClass = interpreter.commandClasses[commandName]
        if (commandClass == null) {
            stderr.println("Dispatching command not found: $commandToDispatch")
            return ExitCode.USAGE
        }

        return dispatch(commandClass, listOf(commandName))
    }
}