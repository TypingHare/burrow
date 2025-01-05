package burrow.carton.core.command.furnishing

import burrow.carton.core.Core
import burrow.kernel.terminal.*

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
        val core = use(Core::class)
        val uniqueAvailableFurnishingId =
            renovator.getUniqueAvailableFurnishingId(furnishingName)

        renovator.furnishingIds.add(uniqueAvailableFurnishingId)
        renovator.save()

        if (!core.rebuildChamber(stderr)) {
            return ExitCode.SOFTWARE
        }
        stdout.println("Furnishing added: $uniqueAvailableFurnishingId")

        return ExitCode.OK
    }
}