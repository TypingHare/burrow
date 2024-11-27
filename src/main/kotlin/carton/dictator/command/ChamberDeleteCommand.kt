package burrow.carton.dictator.command

import burrow.carton.dictator.Dictator
import burrow.kernel.Burrow
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "chamber.delete",
    description = ["Deletes a chamber blueprint."]
)
class ChamberDeleteCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "<name>",
        description = ["The name of the chamber blueprint to delete."]
    )
    private var chamberName = ""

    override fun call(): Int {
        if (chamberName == Burrow.Standard.ROOT_CHAMBER_NAME) {
            stderr.println("Deleting the root chamber blueprint is not allowed.")
            return ExitCode.USAGE
        }

        val dictator = use(Dictator::class)
        val chamberDirs = dictator.getAllChamberDirs()
        val chamberDir = chamberDirs.find { it.name == chamberName }
        if (chamberDir == null) {
            val coloredChamberName =
                palette.color(chamberName, Burrow.Highlights.CHAMBER)
            stderr.println("Unable to find chamber blueprint for $coloredChamberName")
            return ExitCode.USAGE
        }

        chamberDir.deleteRecursively()

        return ExitCode.OK
    }
}