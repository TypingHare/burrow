package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.HoardPair
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "values",
    description = ["Displays stored values based on the specified key or lists all values if no key is provided."]
)
class PairValues(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The key used to filter stored values."],
        defaultValue = ""
    )
    private var key = ""

    override fun call(): Int {
        val hoard = use(Hoard::class)
        val hoardPair = use(HoardPair::class)
        if (key.isEmpty()) {
            hoard.entryStore.filterNotNull().forEach {
                stdout.println(
                    palette.color(
                        hoardPair.getValue(it),
                        Hoard.Highlights.VALUE
                    )
                )
            }

            return ExitCode.OK
        }

        hoardPair.idSetStore[key]?.forEach {
            stdout.println(
                palette.color(
                    hoardPair.getValue(hoard[it]),
                    Hoard.Highlights.VALUE
                )
            )
        }

        return ExitCode.OK
    }
}