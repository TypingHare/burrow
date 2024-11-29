package burrow.carton.hay.command

import burrow.carton.hay.Hay
import burrow.carton.standard.command.ChamberRebuildCommand
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "path.remove",
    description = ["Removes a path."]
)
class PathRemoveCommand(data: CommandData) : Command(data) {
    @Parameters(index = "0", description = ["Path to remove."])
    private var path = ""

    override fun call(): Int {
        val newPathList = config.getNotNull<List<String>>(Hay.ConfigKey.PATH)
            .toMutableList()
            .apply { remove(path) }
        config.set(Hay.ConfigKey.PATH, newPathList)

        return dispatch(
            ChamberRebuildCommand::class,
            listOf("--preserve-config")
        )
    }
}