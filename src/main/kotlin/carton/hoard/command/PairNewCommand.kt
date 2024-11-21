package burrow.carton.hoard.command

import burrow.carton.hoard.HoardPair
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "new",
    description = ["Creates a new entry."]
)
class PairNewCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The key of the new entry."]
    )
    private var key = ""

    @Parameters(
        index = "1",
        description = ["The value of the new entry."]
    )
    private var value = ""

    override fun call(): Int {
        val entry = use(HoardPair::class).createEntry(key, value)

        return dispatch(EntryCommand::class, listOf(entry.id))
    }
}