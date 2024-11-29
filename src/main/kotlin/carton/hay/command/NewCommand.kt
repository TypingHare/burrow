package burrow.carton.hay.command

import burrow.carton.hay.Hay
import burrow.carton.hay.MultipleAbsolutePathsException
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters
import java.util.concurrent.atomic.AtomicInteger

@CommandLine.Command(
    name = "new",
    description = ["Creates a new path pair."]
)
class NewCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "<relative-path>",
        description = ["The relative path to add."],
    )
    private var relativePath = ""

    @Parameters(
        index = "1",
        paramLabel = "<absolute-path>",
        description = ["The associated absolute path."],
        defaultValue = ""
    )
    private var absolutePath = ""

    override fun call(): Int {
        val hay = use(Hay::class)
        if (absolutePath.isNotBlank()) {
            hay.createEntry(relativePath, absolutePath)

            return dispatch(InfoCommand::class, listOf(relativePath))
        }

        try {
            hay.createEntry(relativePath)
        } catch (ex: MultipleAbsolutePathsException) {
            stderr.println("Multiple git repositories found: ")

            val index = AtomicInteger(0)
            ex.candidateAbsolutePaths.forEach {
                stderr.println("$[${index.getAndIncrement()}] $it")
            }

            return ExitCode.USAGE
        }

        return dispatch(InfoCommand::class, listOf(relativePath))
    }
}