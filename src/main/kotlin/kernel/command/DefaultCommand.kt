package burrow.kernel.command

import picocli.CommandLine

@CommandLine.Command(
    name = Processor.DEFAULT_COMMAND_NAME,
    description = [
        "The default command that is executed when no command is specified."
    ]
)
class DefaultCommand(data: CommandData) : Command(data) {
    @CommandLine.Option(
        names = ["-n", "--name"],
        description = ["Display the name of the current chamber."],
        defaultValue = "false"
    )
    var showName = false

    @CommandLine.Option(
        names = ["-h", "--help"],
        description = ["Display the help information."],
        defaultValue = "false"
    )
    var showHelp = false

    override fun call(): Int {
        if (showName || showHelp) {
            val name = chamber.name
            stdout.println(name)
        }

        return CommandLine.ExitCode.OK
    }
}