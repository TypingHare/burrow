package burrow.carton.haystack.command

import burrow.carton.haystack.Haystack
import burrow.carton.hoard.HoardPair
import burrow.kernel.terminal.*
import java.io.File

@BurrowCommand(
    name = "info",
    header = ["Displays the information of an entry."],
)
class InfoCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The name of the entry."],
    )
    private var name = ""

    private val hoardPair = use(HoardPair::class)

    override fun call(): Int {
        val entry = hoardPair.getFirstEntryOrThrow(name)
        val absolutePath = entry.get<String>(Haystack.EntryKey.ABSOLUTE_PATH)!!
        stdout.println("$name -> $absolutePath")

        // Print some information
        val file = File(absolutePath)
        val existing = file.exists()
        stdout.println("existing: $existing")
        if (existing) {
            stdout.println("type: " + if (file.isDirectory()) "file" else "directory")
        }

        return ExitCode.OK
    }
}