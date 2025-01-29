package burrow.carton.hoard.command.encryption

import burrow.carton.hoard.HoardEncryption
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "hoard.save",
    header = ["Saves the hoard."]
)
class HoardSaveCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(HoardEncryption::class).save()
        return ExitCode.OK
    }
}