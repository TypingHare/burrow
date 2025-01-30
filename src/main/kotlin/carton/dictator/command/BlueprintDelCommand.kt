package burrow.carton.dictator.command

import burrow.carton.dictator.Dictator
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "blueprint.del",
    header = ["Deletes a chamber blueprint."]
)
class BlueprintDelCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "name",
        description = ["The name of the chamber blueprint to delete."]
    )
    private var chamberName = ""

    override fun call(): Int {
        if (chamberName == ChamberShepherd.ROOT_CHAMBER_NAME) {
            stderr.println("Deleting the root chamber blueprint is not allowed.")
            return ExitCode.USAGE
        }

        // Destroy the chamber if it has been built
        try {
            chamberShepherd.destroyChamber(chamberName)
        } catch (_: Exception) {
        }

        val dictator = use(Dictator::class)
        val chamberDirs = dictator.getBlueprintDirs()
        val chamberDir = chamberDirs.find { it.name == chamberName }
        if (chamberDir == null) {
            stderr.println("Unable to find chamber blueprint for $chamberName")
            return ExitCode.USAGE
        }

        chamberDir.deleteRecursively()

        return ExitCode.OK
    }
}