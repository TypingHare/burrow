package burrow.carton.hoard.command.pair

import burrow.carton.hoard.HoardPair
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "keys",
    description = ["Displays all keys."]
)
class PairKeysCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val hoardPair = use(HoardPair::class)
        val keys = hoardPair.idSetStore.keys
        keys.forEach { stdout.println(it) }

        return ExitCode.OK
    }
}