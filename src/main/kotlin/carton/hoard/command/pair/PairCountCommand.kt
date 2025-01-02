package burrow.carton.hoard.command.pair

import burrow.carton.hoard.HoardPair
import burrow.carton.hoard.command.CountCommand
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "count",
    description = [""]
)
class PairCountCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The key to count."],
        defaultValue = ""
    )
    private var key = ""

    override fun call(): Int {
        if (key.isEmpty()) {
            return dispatch(CountCommand::class)
        }

        val idSet = use(HoardPair::class).idSetStore[key]
        stdout.println(idSet?.size ?: 0)

        return ExitCode.OK
    }
}