package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.checkId
import burrow.carton.hoard.checkPairs
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "set",
    description = ["Sets properties for a specific hoard entry by ID."]
)
class SetCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = [
            "The unique ID of the entry to modify. Must be a positive integer."
        ]
    )
    var id = 0

    @Parameters(
        index = "1..*",
        description = [
            "The keys and values to set as properties for the specified entry."
        ]
    )
    private var pairs: Array<String> = emptyArray()

    override fun call(): Int {
        if (!checkId(id, stderr)) {
            return ExitCode.USAGE
        }

        if (!checkPairs(pairs, stderr)) {
            return ExitCode.USAGE
        }

        val properties = mutableMapOf<String, String>()
        for (i in pairs.indices step 2) {
            properties[pairs[i]] = pairs[i + 1]
        }

        val hoard = use(Hoard::class)
        val entry = hoard[id]
        hoard.setProperties(entry, properties)

        return dispatch(EntryCommand::class, listOf(id.toString()))
    }
}