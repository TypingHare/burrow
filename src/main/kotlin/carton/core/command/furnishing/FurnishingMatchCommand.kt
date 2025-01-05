package burrow.carton.core.command.furnishing

import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "furnishing.match",
    header = ["Matches a furnishing ID."]
)
class FurnishingMatchCommand(data: CommandData) : Command(data) {
    @BurrowCommand(
        name = "match",
        description = ["The furnishing name used to match."]
    )
    private var furnishingName = ""

    override fun call(): Int {
        val furnishingIds = renovator.getFurnishingIds(furnishingName)
        when (furnishingIds.size) {
            0 -> stdout.println("No furnishings are matched")
            1 -> stdout.println(furnishingIds[0])
            else -> {
                stdout.println("Multiple furnishings are matched:")
                furnishingIds.forEach { stdout.println("  - $it") }
            }
        }

        return ExitCode.OK
    }
}