package burrow.carton.core.command.furnishing

import burrow.carton.core.command.chamber.ChamberRebuildCommand
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Parameters

@BurrowCommand(
    name = "furnishing.add",
    header = ["Adds a new furnishing."],
)
class FurnishingAddCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = [
            "The name of the furnishing to add."
        ]
    )
    private var furnishingName = ""

    override fun call(): Int {
        val uniqueAvailableFurnishingId =
            renovator.getUniqueAvailableFurnishingId(furnishingName)

        renovator.furnishingIds.add(uniqueAvailableFurnishingId)
        renovator.save()

        return dispatch(ChamberRebuildCommand::class)
    }
}