package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "hoard.clear",
    header = ["Clear all entries."]
)
class HoardClearCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(Hoard::class).storage.clear()
        return ExitCode.OK
    }
}