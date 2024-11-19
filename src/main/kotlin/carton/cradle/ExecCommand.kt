package burrow.carton.cradle

import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "exec",
    description = ["Executes a command."]
)
class ExecCommand(data: CommandData) : Command(data) {
    @Parameters(index = "0", description = ["Command to execute."])
    private var command = ""

    override fun call(): Int {
        val cradle = use(Cradle::class)
        return cradle.executeCommand(command, environment, stdout, stderr)
    }
}