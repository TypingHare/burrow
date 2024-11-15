package burrow.furnishing.hoard.command

import burrow.furnishing.hoard.Entry
import burrow.furnishing.hoard.EntryNotFoundException
import burrow.furnishing.hoard.Hoard
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine

@CommandLine.Command(
    name = "entry",
    description = ["Finds an entry by its associated ID and displays it."]
)
class EntryCommand(data: CommandData) : Command(data) {
    @CommandLine.Parameters(index = "0")
    private var id: Int = 0

    @CommandLine.Option(
        names = ["-p", "--properties"],
        description = ["Displays it as an entry object."],
        defaultValue = "false"
    )
    private var asProperties: Boolean = false

    @Throws(EntryNotFoundException::class)
    override fun call(): Int {
        if (id <= 0) {
            stderr.println("Invalid entry ID: $id")
            return CommandLine.ExitCode.USAGE
        }

        val entry = use(Hoard::class)[id]
        if (asProperties) printProperties(entry)

        return CommandLine.ExitCode.OK
    }

    private fun printProperties(entry: Entry) {
        stdout.println("{")
        for ((key, value) in entry.properties) {
            stdout.println("    $key: \"$value\"")
        }
        stdout.println("}")
    }
}