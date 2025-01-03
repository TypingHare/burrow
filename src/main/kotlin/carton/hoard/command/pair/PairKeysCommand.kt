package burrow.carton.hoard.command.pair

import burrow.carton.hoard.HoardPair
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "keys",
    header = ["Displays all keys."]
)
class PairKeysCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val hoardPair = use(HoardPair::class)
        val keys = hoardPair.idSetStore.keys
        keys.forEach { stdout.println(it) }

        return ExitCode.OK
    }
}