package burrow.carton.core.command.chamber

import burrow.carton.core.Core
import burrow.carton.core.command.furnishing.FurnishingListCommand
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode
import java.nio.file.Path

@BurrowCommand(
    name = "chamber",
    header = ["Display the information of the chamber."]
)
class ChamberCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val description = config.getNotNull<String>(Core.ConfigKey.DESCRIPTION)
        stdout.println("${chamber.name} - $description")

        // Involved cartons
        val furnishingIds = renovator.furnishings.keys
        val cartonPathSet = mutableSetOf<Path>().apply {
            for (furnishingId in furnishingIds) {
                this.add(warehouse.furnishingIdToCarton[furnishingId]!!.path)
            }
        }

        stdout.println()
        stdout.println("[Cartons]")
        cartonPathSet.map { it.toString() }.sorted().map(stderr::println)

        stdout.println()
        stdout.println("[Furnishings]")
        dispatch(FurnishingListCommand::class)

        return ExitCode.OK
    }
}