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
            "The ID of the furnishing to add."
        ]
    )
    private var furnishingId = ""

    override fun call(): Int {
        val core = use(Core::class)
        val availableFurnishingIds = core
            .getAvailableFurnishingClasses()
            .map { it.java.name }
            .toSet()
        if (furnishingId !in availableFurnishingIds) {
            stderr.println("Furnishing not available: $furnishingId")
            return ExitCode.USAGE
        }

        renovator.furnishingIds.add(furnishingId)
        renovator.save()

        if (!core.rebuildChamber(stderr)) {
            return ExitCode.SOFTWARE
        }
        stdout.println("Furnishing added: $furnishingId")

        return ExitCode.OK
    }
}