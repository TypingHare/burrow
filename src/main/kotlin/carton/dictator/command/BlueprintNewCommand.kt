package burrow.carton.dictator.command

import burrow.carton.core.Core
import burrow.carton.dictator.Dictator
import burrow.kernel.terminal.*
import java.nio.file.Files

@BurrowCommand(
    name = "blueprint.new",
    header = ["Creates a new chamber blueprint."]
)
class BlueprintNewCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The name of the chamber to create."]
    )
    private var chamberName = ""

    @Parameters(
        index = "1",
        description = ["The description of the chamber."],
        defaultValue = Core.Default.DESCRIPTION
    )
    private var description = ""

    override fun call(): Int {
        val chamberPath = chamberShepherd.getPath().resolve(chamberName)
        if (Files.exists(chamberPath)) {
            stderr.println("Chamber blueprint already exists: $chamberName ($chamberPath)")
            return ExitCode.USAGE
        }

        if (!chamberPath.toFile().mkdirs()) {
            stderr.println("Failed to create chamber root directory: $chamberPath")
            return ExitCode.SOFTWARE
        }

        use(Dictator::class).apply {
            createFurnishingsJson(chamberPath)
            createConfigJson(chamberPath, description)
        }
        stdout.println("Created chamber blueprint: $chamberName ($chamberPath)")

        return ExitCode.OK
    }
}