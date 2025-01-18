package burrow.carton.shell.command

import burrow.carton.shell.Shell
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode
import java.nio.file.Files

@BurrowCommand(
    name = "shell.content",
    header = ["Displays the content of the shell file."],
)
class ShellContentCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val binFile = use(Shell::class).getShellFilePath()
        stdout.println(Files.readString(binFile))

        return ExitCode.OK
    }
}