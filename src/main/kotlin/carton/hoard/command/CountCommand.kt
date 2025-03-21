package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "count",
    header = ["Displays the number of entries in the hoard."]
)
class CountCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        stdout.println(use(Hoard::class).storage.size)
        return ExitCode.OK
    }
}