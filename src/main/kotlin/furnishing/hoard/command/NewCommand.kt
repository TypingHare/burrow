package burrow.furnishing.hoard.command

import burrow.furnishing.hoard.Hoard
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine

@CommandLine.Command(
    name = "new",
    description = ["Creates a new entry."],
)
class NewCommand(data: CommandData) : Command(data) {
    @CommandLine.Parameters(arity = "0..*")
    private var params: Array<String> = emptyArray()

    override fun call(): Int {
        if (params.isEmpty() && params.size % 2 == 1) {
            stderr.println("Invalid number of arguments: ${params.size}")
            return CommandLine.ExitCode.USAGE
        }

        val properties = mutableMapOf<String, String>()
        for (i in 0 until params.size / 2) {
            properties[params[i * 2]] = params[i * 2 + 1]
        }

        val entry = use(Hoard::class).create(properties)
        stdout.println(entry.id)

        return CommandLine.ExitCode.OK;
    }
}