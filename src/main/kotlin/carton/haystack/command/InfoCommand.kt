package burrow.carton.haystack.command

import burrow.carton.haystack.Haystack
import burrow.kernel.terminal.*
import java.io.File

@BurrowCommand(
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
        val entry = use(Haystack::class).getEntry(relativePath)
        val absolutePath = entry.get<String>(Haystack.EntryKey.ABSOLUTE_PATH)!!
        stdout.println("$relativePath -> $absolutePath")

        // Print file information
        val file = File(absolutePath)
        val existing = file.exists().toString()
        val isDirectory = file.isDirectory().toString()
        stdout.println("Existing: $existing")
        stdout.println("Is directory: $isDirectory")

        return ExitCode.OK
    }
}