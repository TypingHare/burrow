package burrow.carton.git.command

import burrow.carton.hay.command.NewCommand
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "new",
    description = ["Creates a new repository entry."]
)
class NewCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The relative path of the repository to add."],
    )
    private var path = ""

    override fun call(): Int {
        return dispatch(NewCommand::class, listOf(path))
    }
}