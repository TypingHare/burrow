package burrow.carton.haystack.command

import burrow.carton.core.command.chamber.ChamberRebuildCommand
import burrow.carton.haystack.Haystack
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Parameters

@BurrowCommand(
    name = "path.add",
    header = ["Adds a path."]
)
class PathAddCommand(data: CommandData) : Command(data) {
    @Parameters(index = "0", description = ["Path to add."])
    private var path = ""

    override fun call(): Int {
        use(Haystack::class).getPathList().apply { add(path) }
        return dispatch(ChamberRebuildCommand::class)
    }
}