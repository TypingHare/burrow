package burrow.carton.shell.command

import burrow.carton.shell.Shell
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode
import kotlin.io.path.deleteIfExists

@BurrowCommand(
    name = "shell.del",
    header = ["Deletes the shell file if exists."]
)
class ShellDelCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(Shell::class).getShellFilePath().deleteIfExists()
        return ExitCode.OK
    }
}