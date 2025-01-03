package burrow.client.cli.command

import burrow.client.cli.BurrowCli
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = BurrowCli.CliCommand.CLEAR,
    header = ["Clears the terminal."]
)
class ClearCommand(cli: BurrowCli) : BurrowCliCommand(cli) {
    override fun call(): Int {
        cli.terminal!!.apply {
            writer().print("\u001B[H\u001B[2J")
            flush()
        }

        return ExitCode.OK
    }
}