package burrow.furnishing.hoard.command

import burrow.furnishing.hoard.Hoard
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine

@CommandLine.Command(
    name = "exist",
    description = [""]
)
class ExistCommand(data: CommandData) : Command(data) {
    @CommandLine.Parameters(index = "0", description = ["The ID of the entry."])
    private var id: Int = 0

    override fun call(): Int {
        if (id <= 0) {
            stderr.println("Invalid entry ID: $id")
            return CommandLine.ExitCode.USAGE
        }

        val isExist = use(Hoard::class).exists(id)
        stdout.println(if (isExist) "true" else "false")

        return CommandLine.ExitCode.OK
    }
}