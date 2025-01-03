package burrow.carton.hoard.command.pair

import burrow.carton.hoard.HoardPair
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "values",
    header = ["Displays all values associated with a specified key."],
)
class PairValuesCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The key."]
    )
    private var key = ""

    override fun call(): Int {
        val hoardPair = use(HoardPair::class)
        val entries = hoardPair.getEntries(key)
        for (entry in entries) {
            stdout.println(hoardPair.getValue<Any>(entry))
        }

        return ExitCode.OK
    }
}