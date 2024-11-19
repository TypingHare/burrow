import burrow.client.cli.BurrowClientCli
import burrow.client.cli.BurrowClientCliCommand
import picocli.CommandLine
import picocli.CommandLine.ExitCode

@CommandLine.Command(
    name = "exit",
    description = ["Exits Burrow command line."]
)
class ExitCommand(cli: BurrowClientCli) : BurrowClientCliCommand(cli) {
    override fun call(): Int {
        cli.exit()
        return ExitCode.OK
    }
}