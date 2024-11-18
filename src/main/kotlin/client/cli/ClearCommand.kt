package burrow.client.cli

import picocli.CommandLine
import picocli.CommandLine.ExitCode

@CommandLine.Command(
    name = "clear",
    description = ["Clears the screen."]
)
class ClearCommand(cli: BurrowClientCli) : BurrowClientCliCommand(cli) {
    override fun call(): Int {
        val terminal = cli.terminal!!
        terminal.writer().print("\u001B[H\u001B[2J")
        terminal.flush();
        return ExitCode.OK
    }
}