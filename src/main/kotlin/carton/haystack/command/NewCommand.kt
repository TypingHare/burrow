package burrow.carton.haystack.command

import burrow.carton.haystack.Haystack
import burrow.carton.haystack.MultipleAbsolutePathsException
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "new",
    header = ["Creates a new entry."]
)
class NewCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The name of the entry to add."],
    )
    private var name = ""

    @Parameters(
        index = "1",
        description = ["The associated absolute path."],
        defaultValue = ""
    )
    private var absolutePath = ""

    override fun call(): Int {
        return when (absolutePath.isBlank()) {
            true -> addByRelativePath()
            false -> addByAbsolutePath()
        }
    }

    private fun addByRelativePath(): Int {
        val haystack = use(Haystack::class)
        try {
            haystack.createEntry(name)
        } catch (ex: MultipleAbsolutePathsException) {
            stderr.println("Multiple absolute paths found: ")
            ex.candidateAbsolutePaths.forEach { stderr.println(it) }

            return ExitCode.USAGE
        }

        return dispatch(InfoCommand::class, listOf(name))
    }

    private fun addByAbsolutePath(): Int {
        val haystack = use(Haystack::class)
        haystack.createEntry(name, absolutePath)
        return dispatch(InfoCommand::class, listOf(name))
    }
}