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
        names = ["--name", "-n"],
        description = ["Displays the name of the current chamber."],
    )
    private var showName = false

    @CommandLine.Option(
        names = ["--help", "-h"],
        description = ["Displays the help information."],
    )
    private var showHelp = false

    override fun call(): Int {
        if (showName || showHelp) {
            stdout.println(chamber.name)
        }

        return CommandLine.ExitCode.OK
    }
}