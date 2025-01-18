package burrow.carton.haystack.command

import burrow.carton.core.command.chamber.RebuildCommand
import burrow.carton.haystack.Haystack
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Parameters

@BurrowCommand(
    name = "path.remove",
    header = ["Removes a path."]
)
class PathRemoveCommand(data: CommandData) : Command(data) {
    @Parameters(index = "0", description = ["Path to remove."])
    private var path = ""

    override fun call(): Int {
        use(Haystack::class).getPathList().apply { remove(path) }
        return dispatch(RebuildCommand::class)
    }
}