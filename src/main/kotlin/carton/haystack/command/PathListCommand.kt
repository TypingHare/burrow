package burrow.carton.haystack.command

import burrow.carton.haystack.Haystack
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "path",
    header = ["Displays all paths."]
)
class PathListCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(Haystack::class).getPathList().forEach(stdout::println)
        return ExitCode.OK
    }
}