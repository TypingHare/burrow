package burrow.carton.dictator.command

import burrow.kernel.Burrow
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "chamber.destroy",
    description = ["Destroys a chamber."]
)
class ChamberDestroyCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = [
            "The name of the chamber to destroy."
        ]
    )
    var chamberName = ""

    override fun call(): Int {
        if (chamberName == Burrow.Standard.ROOT_CHAMBER_NAME) {
            stderr.println("You cannot destroy the root chamber!")
            return ExitCode.USAGE
        }

        val chamberShepherd = burrow.chamberShepherd
        if (!chamberShepherd.chambers.containsKey(chamberName)) {
            stderr.println("Chamber has yet been built: $chamberName")
            return ExitCode.USAGE
        }

        burrow.chamberShepherd.destroyChamber(chamberName)

        return ExitCode.OK
    }
}