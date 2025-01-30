package burrow.carton.cradle.command

import burrow.carton.cradle.Cradle
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Parameters

@BurrowCommand(
    name = "exec",
    header = ["Executes a shell command."]
)
class ExecCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["Shell command to execute."]
    )
    private var shellCommand = ""

    override fun call(): Int {
        val cradle = use(Cradle::class)
        return cradle.executeCommand(shellCommand, this)
    }
}