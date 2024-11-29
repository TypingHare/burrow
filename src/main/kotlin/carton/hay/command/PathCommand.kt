package burrow.carton.hay.command

import burrow.carton.hay.Hay
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode

@CommandLine.Command(
    name = "path",
    description = ["Displays all paths."]
)
class PathCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val paths = use(Hay::class).getPaths()
        paths.forEach { stdout.println(it) }

        return ExitCode.OK
    }
}