package burrow.carton.cradle.command

import burrow.carton.cradle.Cradle
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "exec",
    description = ["Executes a command."]
)
class ExecCommand(data: CommandData) : Command(data) {
    @Parameters(index = "0", description = ["Command to execute."])
    private var command = ""

    override fun call(): Int {
        val cradle = use(Cradle::class)
        return cradle.executeCommand(command, data.environment, stdout, stderr)
    }
}