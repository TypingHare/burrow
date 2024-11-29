package burrow.carton.git.command

import burrow.carton.hay.command.InfoCommand
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "info",
    description = ["Displays the information of a repository."]
)
class InfoCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "<relative-path>",
        description = ["The relative path of the repository to add."],
    )
    private var relativePath = ""

    override fun call(): Int {
        return dispatch(InfoCommand::class, listOf(relativePath))
    }
}