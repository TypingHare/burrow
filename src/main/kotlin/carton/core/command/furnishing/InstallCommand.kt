package burrow.carton.core.command.furnishing

import burrow.carton.core.command.chamber.RebuildCommand
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Parameters

@BurrowCommand(
    name = "install",
    header = ["Installs a new furnishing."],
)
class InstallCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0..*",
        description = ["The names of the furnishing to install."]
    )
    private var names: Array<String> = emptyArray()

    override fun call(): Int {
        names.map { renovator.getUniqueAvailableFurnishingId(it) }
            .toSet()
            .let { idSet -> renovator.furnishingIds.addAll(idSet) }
        renovator.save()

        return dispatch(RebuildCommand::class)
    }
}