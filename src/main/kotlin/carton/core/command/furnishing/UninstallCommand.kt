package burrow.carton.core.command.furnishing

import burrow.carton.core.command.chamber.RebuildCommand
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Parameters

@BurrowCommand(
    name = "uninstall",
    header = ["Uninstalls a furnishing."]
)
class UninstallCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0..*",
        description = ["The names of the furnishing to uninstall."]
    )
    private var names: Array<String> = emptyArray()

    override fun call(): Int {
        names.map { renovator.getUniqueFurnishingId(it) }
            .toSet()
            .let { idSet -> renovator.furnishingIds.removeAll(idSet) }
        renovator.save()

        return dispatch(RebuildCommand::class)
    }
}