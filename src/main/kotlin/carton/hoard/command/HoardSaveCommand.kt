package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "hoard.save",
    header = ["Saves the hoard immediately."]
)
class HoardSaveCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(Hoard::class).save()
        return ExitCode.OK
    }
}