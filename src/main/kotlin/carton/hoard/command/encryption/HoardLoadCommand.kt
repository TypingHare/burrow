package burrow.carton.hoard.command.encryption

import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "hoard.load",
    header = ["Decrypts the encrypted hoard file and loads it."]
)
class HoardLoadCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        return ExitCode.OK
    }
}