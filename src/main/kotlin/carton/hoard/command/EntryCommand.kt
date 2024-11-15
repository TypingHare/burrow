package burrow.carton.hoard.command

import burrow.carton.hoard.EntryNotFoundException
import burrow.carton.hoard.Hoard
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import java.io.PrintWriter

@CommandLine.Command(
    name = "entry",
    description = ["Finds an entry by its associated ID and displays it."]
)
class EntryCommand(data: CommandData) : Command(data) {
    @CommandLine.Parameters(index = "0")
    private var id: Int = 0

    @CommandLine.Option(
        names = ["-r", "--raw"],
        description = ["Displays the raw properties."],
        defaultValue = "false"
    )
    private var raw: Boolean = false

    @Throws(EntryNotFoundException::class)
    override fun call(): Int {
        if (id <= 0) {
            stderr.println("Invalid entry ID: $id")
            return CommandLine.ExitCode.USAGE
        }

        val entry = use(Hoard::class)[id]
        val properties = if (raw) entry.properties else {
            use(Hoard::class).convertStoreToProperties(entry)
        }
        printProperties(properties, stdout)

        return CommandLine.ExitCode.OK
    }

    private fun printProperties(
        properties: Map<String, String>,
        writer: PrintWriter
    ) {
        writer.println(palette.color("{", Hoard.Highlights.BRACE))
        for ((key, value) in properties) {
            val coloredKey = palette.color(key, Hoard.Highlights.KEY)
            val coloredValue =
                palette.color("\"$value\"", Hoard.Highlights.VALUE)
            writer.println("${" ".repeat(4)}$coloredKey: $coloredValue")
        }
        writer.println(palette.color("}", Hoard.Highlights.BRACE))
    }
}