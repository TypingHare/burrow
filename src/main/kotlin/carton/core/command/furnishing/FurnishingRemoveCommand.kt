package burrow.carton.core.command.furnishing

import burrow.carton.core.Core
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "furnishing.remove",
    header = ["Removes a furnishing from the chamber."]
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
        renovator.save()
        if (!use(Core::class).rebuildChamber(stderr)) {
            return ExitCode.SOFTWARE
        }

        stdout.println("Furnishing removed: $furnishingId")

        return ExitCode.OK
    }
}