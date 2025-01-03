package burrow.carton.haystack.command

import burrow.carton.haystack.Haystack
import burrow.carton.haystack.MultipleAbsolutePathsException
import burrow.kernel.terminal.*

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
        val haystack = use(Haystack::class)
        if (absolutePath.isNotBlank()) {
            haystack.createEntry(relativePath, absolutePath)

            return dispatch(InfoCommand::class, listOf(relativePath))
        }

        try {
            haystack.createEntry(relativePath)
        } catch (ex: MultipleAbsolutePathsException) {
            stderr.println("Multiple absolute paths found: ")
            ex.candidateAbsolutePaths.forEach { stderr.println(it) }

            return ExitCode.USAGE
        }

        return dispatch(InfoCommand::class, listOf(relativePath))
    }
}