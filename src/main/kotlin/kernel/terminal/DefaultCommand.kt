package burrow.kernel.terminal

@BurrowCommand(
    name = "",
    description = [
        "The default command that is executed when no command is specified."
    ]
)
class DefaultCommand(data: CommandData) : Command(data) {
    @Option(
        names = ["--help", "-h"],
        description = ["Displays the help information."],
    )
    private var showHelp = false

    override fun call(): Int {
        if (showHelp) {
            stdout.println(chamber.name)
        }

        return ExitCode.OK
    }
}