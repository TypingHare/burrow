package burrow.carton.shell.command

import burrow.carton.shell.Shell
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "shell.path",
    description = ["Displays the path to the shell file."],
)
class ShellPathCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        stdout.println(use(Shell::class).getBinFile())

        return ExitCode.OK
    }
}