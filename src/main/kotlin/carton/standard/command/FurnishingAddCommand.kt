package burrow.carton.standard.command

import burrow.carton.standard.Standard
import burrow.kernel.Burrow
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "furnishing.add",
    description = ["Adds a new furnishing."],
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
        val standard = use(Standard::class)
        val availableFurnishingIds = standard
            .getAvailableFurnishingClasses()
            .map { it.java.name }
            .toSet()
        if (furnishingId !in availableFurnishingIds) {
            stderr.println("Furnishing not available: $furnishingId")
            return ExitCode.USAGE
        }

        val originalFurnishingIds = renovator.furnishings.keys.toSet()
        val furnishingIds = originalFurnishingIds.toMutableSet()
        furnishingIds.add(furnishingId)
        renovator.saveFurnishingIds(furnishingIds)

        val coloredFurnishingId =
            palette.color(furnishingId, Burrow.Highlights.FURNISHING)
        stdout.println("Furnishing added: $coloredFurnishingId")
        stdout.println("Rebuilding the chamber...")

        if (!use(Standard::class).rebuildChamberAfterUpdatingFurnishingList(
                originalFurnishingIds, stderr
            )
        ) {
            return ExitCode.SOFTWARE
        }

        stdout.println("Restarted successfully!")
        return ExitCode.OK
    }
}