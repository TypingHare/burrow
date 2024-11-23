package burrow.carton.git

import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters
import java.util.concurrent.atomic.AtomicInteger

@CommandLine.Command(
    name = "new",
    description = ["Creates a new repository entry."]
)
class NewCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The relative path of the repository to add."],
    )
    private var path = ""

    override fun call(): Int {
        try {
            use(Git::class).createRepositoryEntry(path)
        } catch (ex: MultipleGitRepositoriesException) {
            stderr.println("Multiple git repositories found: ")

            val index = AtomicInteger(0)
            for (absolutePath in ex.absolutePaths) {
                stderr.println("$[${index.getAndIncrement()}] $absolutePath")
            }

            return ExitCode.USAGE
        }

        return ExitCode.OK
    }
}