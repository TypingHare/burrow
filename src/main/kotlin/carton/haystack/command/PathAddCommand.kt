package burrow.carton.haystack.command

import burrow.carton.core.command.ChamberRebuildCommand
import burrow.carton.haystack.Haystack
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Parameters

@BurrowCommand(
    name = "path.add",
    description = ["Adds a path."]
)
class PathAddCommand(data: CommandData) : Command(data) {
    @Parameters(index = "0", description = ["Path to add."])
    private var path = ""

    override fun call(): Int {
        val newPathList =
            config.getNotNull<List<String>>(Haystack.ConfigKey.PATH)
                .toMutableList()
                .apply { add(path) }
        config[Haystack.ConfigKey.PATH] = newPathList

        return dispatch(ChamberRebuildCommand::class)
    }
}