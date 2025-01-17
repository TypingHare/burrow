package burrow.carton.core.command.furnishing

import burrow.carton.core.command.chamber.ChamberRebuildCommand
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Parameters

@BurrowCommand(
    name = "furnishing.remove",
    header = ["Removes a furnishing from the chamber."]
)
class FurnishingRemoveCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = [
            "The name of the furnishing to remove."
        ]
    )
    private var name = ""

    override fun call(): Int {
        val furnishingId = renovator.getUniqueFurnishingId(name)
        renovator.furnishingIds.remove(furnishingId)
        renovator.save()

        return dispatch(ChamberRebuildCommand::class)
    }
}