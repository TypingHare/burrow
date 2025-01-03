package burrow.carton.shell.command

import burrow.carton.shell.Shell
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode
import java.nio.file.Files

@BurrowCommand(
    name = "shell.content",
    description = ["Displays the shell file content."],
)
class ShellContentCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val binFile = use(Shell::class).getBinFile()
        stdout.println(Files.readString(binFile))

        return ExitCode.OK
    }
}