package burrow.carton.dictator.command

import burrow.carton.dictator.Dictator
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters
import java.nio.file.Files

@CommandLine.Command(
    name = "chamber.new",
    description = ["Creates a new chamber blueprint."]
)
class ChamberNewCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The name of the chamber to create."]
    )
    private var chamberName = ""

    override fun call(): Int {
        val chamberRootDirPath = burrow.chambersPath.resolve(chamberName)
        if (Files.exists(chamberRootDirPath)) {
            stderr.println("Chamber blueprint already exists: $chamberName ($chamberRootDirPath)")
            return ExitCode.USAGE
        }

        if (!chamberRootDirPath.toFile().mkdirs()) {
            stderr.println("Failed to create chamber root directory: $chamberRootDirPath")
            return ExitCode.SOFTWARE
        }

        use(Dictator::class).createFurnishingsJson(chamberRootDirPath)
        stdout.println("Created chamber blueprint: $chamberName ($chamberRootDirPath)")

        return ExitCode.OK
    }
}