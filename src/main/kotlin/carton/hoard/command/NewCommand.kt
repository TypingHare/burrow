package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.checkPairs
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode

@CommandLine.Command(
    name = "new",
    description = ["Creates a new entry."],
)
class NewCommand(data: CommandData) : Command(data) {
    @CommandLine.Parameters(arity = "0..*")
    private var pairs: Array<String> = emptyArray()

    override fun call(): Int {
        if (!checkPairs(pairs, stderr)) {
            return ExitCode.USAGE
        }

        val properties = mutableMapOf<String, String>()
        for (i in pairs.indices step 2) {
            properties[pairs[i]] = pairs[i + 1]
        }

        val entry = use(Hoard::class).create(properties)

        return dispatch(EntryCommand::class, listOf(entry.id.toString()))
    }
}