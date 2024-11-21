package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode

@CommandLine.Command(
    name = "count",
    description = ["Displays the number of entries in the hoard."]
)
class CountCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        stdout.println(use(Hoard::class).size)

        return ExitCode.OK
    }
}