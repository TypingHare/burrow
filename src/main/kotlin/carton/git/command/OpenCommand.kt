package burrow.carton.git.command

import burrow.carton.hay.command.OpenerExecCommand
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "open",
    description = [""]
)
class OpenCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "<relative-path>",
        description = ["The relative path of the entry."]
    )
    private var relativePath = ""

    override fun call(): Int {
        return dispatch(OpenerExecCommand::class, listOf(relativePath))
    }
}