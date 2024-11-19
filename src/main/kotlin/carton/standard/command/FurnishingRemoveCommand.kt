package burrow.carton.standard.command

import burrow.carton.standard.Standard
import burrow.kernel.Burrow
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "furnishing.remove",
    description = ["Removes a furnishing from the chamber."]
)
class FurnishingRemoveCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = [
            "The ID of the furnishing to remove."
        ]
    )
    private var furnishingId = ""

    override fun call(): Int {
        val originalFurnishingIds = renovator.furnishings.keys.toSet()
        val furnishingIds = originalFurnishingIds.toMutableSet()
        if (furnishingId !in furnishingIds) {
            stderr.println("Furnishing has not been installed: $furnishingId")
            return ExitCode.USAGE
        }

        furnishingIds.remove(furnishingId)
        val coloredFurnishingId =
            palette.color(furnishingId, Burrow.Highlights.FURNISHING)
        renovator.saveFurnishingIds(furnishingIds)
        stdout.println("Furnishing removed: $coloredFurnishingId")
        stdout.println("Rebuilding the chamber...")

        use(Standard::class).rebuildChamberAfterUpdatingFurnishingList(
            originalFurnishingIds, stderr
        )
        stdout.println("Restarted successfully!")

        return ExitCode.OK
    }
}