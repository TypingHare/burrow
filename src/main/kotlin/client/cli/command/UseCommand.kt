package burrow.client.cli.command

import burrow.client.cli.BurrowCli
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.ExitCode
import burrow.kernel.terminal.Parameters

@BurrowCommand(
    name = BurrowCli.CliCommand.USE,
    header = ["Uses a chamber."]
)
class UseCommand(cli: BurrowCli) : BurrowCliCommand(cli) {
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