package burrow.carton.shell.command

import burrow.carton.shell.Shell
import burrow.kernel.terminal.*
import kotlin.io.path.exists

@BurrowCommand(
    name = "shell.new",
    header = ["Creates a new shell file."]
)
class ShellNewCommand(data: CommandData) : Command(data) {
    @Option(
        names = ["--force", "-f"],
        paramLabel = "force",
        description = ["Create a shell file no matter if it already exists."]
    )
    var shouldBeForce: Boolean = false

    override fun call(): Int {
        val bin = use(Shell::class)
        val binFile = bin.getShellFilePath()
        val shellContent = bin.getDefaultShellContent()
        if (shouldBeForce) {
            bin.createShellFile(shellContent)
            stdout.println("Created shell file: $binFile")
        } else {
            if (binFile.exists()) {
                stderr.println("Shell file already exists: $binFile")
                return ExitCode.USAGE
            }
            bin.createShellFile(shellContent)
        }

        return ExitCode.OK
    }
}