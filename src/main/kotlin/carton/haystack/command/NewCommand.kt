package burrow.carton.haystack.command

import burrow.carton.haystack.Haystack
import burrow.carton.haystack.MultipleAbsolutePathsException
import burrow.kernel.terminal.*
import java.util.concurrent.atomic.AtomicInteger

@BurrowCommand(
    name = "new",
    header = ["Creates a new path pair."]
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
        val hay = use(Haystack::class)
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