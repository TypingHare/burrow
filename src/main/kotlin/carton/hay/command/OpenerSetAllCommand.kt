package burrow.carton.hay.command

import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "opener.set-all",
    description = ["Set the opener for all entries."]
)
class OpenerSetAllCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The opener to use."]
    )
    private var opener = ""

    override fun call(): Int {
        return ExitCode.OK
    }
}