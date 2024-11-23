package burrow.carton.git

import burrow.carton.hoard.HoardPair
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "info",
    description = ["Displays the information of a repository."]
)
class InfoCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The relative path of the repository to add."],
    )
    private var path = ""

    override fun call(): Int {
        val hoardPair = use(HoardPair::class)
        val entries = hoardPair.getEntries(path)
        if (entries.isEmpty()) {
            stderr.println("")
            return ExitCode.USAGE
        }

        val entry = entries[0]
        stdout.println(path)
        stdout.println(entry.get<String>(Git.EntryKey.ABSOLUTE_PATH))

        return ExitCode.OK
    }
}