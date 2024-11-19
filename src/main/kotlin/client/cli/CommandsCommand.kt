package burrow.client.cli

import picocli.CommandLine.Command
import picocli.CommandLine.ExitCode

@Command(
    name = "commands",
)
class CommandsCommand(cli: BurrowClientCli) : BurrowClientCliCommand(cli) {
    override fun call(): Int {
        return ExitCode.OK
    }
}