package burrow.client.cli

import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "use",
    description = ["Uses a chamber."]
)
class UseCommand(cli: BurrowClientCli) : BurrowClientCliCommand(cli) {
    @Parameters(
        index = "0",
        description = ["The chamber to use."]
    )
    private var chamberName: String = ""

    override fun call(): Int {
        cli.useChamber(chamberName)
        return ExitCode.OK
    }
}