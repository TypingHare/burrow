package burrow.carton.core.command

import burrow.kernel.terminal.*

@BurrowCommand(
    name = "root",
    header = [
        "Displays the absolute path to the chamber root directory."
    ]
)
class RootCommand(data: CommandData) : Command(data) {
    @Option(
        names = ["--quiet", "-q"],
        description = ["Do not display anything."]
    )
    var quiet: Boolean = false

    override fun call(): Int {
        if (!quiet) {
            stdout.println(chamber.getPath())
        }

        return ExitCode.OK
    }
}