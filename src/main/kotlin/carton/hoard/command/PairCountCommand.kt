package burrow.carton.hoard.command

import burrow.carton.hoard.HoardPair
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
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