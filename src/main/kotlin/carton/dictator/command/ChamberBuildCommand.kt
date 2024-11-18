package burrow.carton.dictator.command

import burrow.carton.dictator.Dictator
import burrow.kernel.Burrow
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "chamber.build",
    description = ["Builds a chamber."]
)
class ChamberBuildCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = [
            "The name of the chamber to build."
        ]
    )
    var chamberName = ""

    override fun call(): Int {
        if (chamberName == Burrow.Standard.ROOT_CHAMBER_NAME) {
            stderr.println("You cannot build the root chamber!")
            return ExitCode.USAGE
        }

        val chamberShepherd = burrow.chamberShepherd
        if (chamberShepherd.chambers.containsKey(chamberName)) {
            stderr.println("Chamber has been built: $chamberName")
            return ExitCode.USAGE
        }

        val chamberNames =
            use(Dictator::class).getAllChamberDirs().map { it.name }.toSet()
        if (chamberName !in chamberNames) {
            val chamberRootDir = burrow.chambersPath.resolve(chamberName)
            stderr.println("Chamber root directory does not exist: $chamberRootDir")
            return ExitCode.USAGE
        }

        burrow.chamberShepherd.buildChamber(chamberName)

        return ExitCode.OK
    }
}