package burrow.carton.hay.command

import burrow.carton.hay.Hay
import burrow.carton.hoard.HoardPair
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters
import java.io.File

@CommandLine.Command(
    name = "info",
    description = ["Prints the information about an entry."],
)
class InfoCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "<relative-path>",
        description = ["The relative path."],
    )
    private var relativePath = ""

    override fun call(): Int {
        val entry = use(Hay::class).getEntry(relativePath)
        if (entry == null) {
            stderr.println("Relative path not found: $relativePath")
            return ExitCode.USAGE
        }

        val absolutePath = entry.get<String>(Hay.EntryKey.ABSOLUTE_PATH)!!
        val coloredRelativePath =
            palette.color(relativePath, HoardPair.Highlights.KEY)
        val coloredAbsolutePath =
            palette.color(absolutePath, HoardPair.Highlights.VALUE)
        stdout.println("$coloredRelativePath ($coloredAbsolutePath)")

        // Print file information
        val file = File(absolutePath)
        val existing = file.exists().toString()
        val isDirectory = file.isDirectory().toString()
        stdout.println("Existing: $existing")
        stdout.println("Is directory: $isDirectory")

        return ExitCode.OK
    }
}