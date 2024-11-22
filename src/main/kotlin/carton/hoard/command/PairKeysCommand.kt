package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.HoardPair
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode

@CommandLine.Command(
    name = "keys",
    description = ["Display all keys."]
)
class PairKeysCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val hoardPair = use(HoardPair::class)
        val keys = hoardPair.idSetStore.keys
        keys.forEach { stdout.println(palette.color(it, Hoard.Highlights.KEY)) }

        return ExitCode.OK
    }
}