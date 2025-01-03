package burrow.client.cli.command

import burrow.client.cli.BurrowCli
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = BurrowCli.CliCommand.EXIT,
    header = ["Exits Burrow command line interface."]
)
class ExitCommand(cli: BurrowCli) : BurrowCliCommand(cli) {
    override fun call(): Int {
        cli.exit()
        return ExitCode.OK
    }
}