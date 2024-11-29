package burrow.carton.hay.command

import burrow.carton.hay.Hay
import burrow.carton.hay.HayOpener
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "opener.set",
    description = ["Sets the opener."]
)
class OpenerSetCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "<relative-path>",
        description = ["The relative path of the entry."]
    )
    private var relativePath = ""

    @Parameters(
        index = "1",
        description = ["The opener to use."]
    )
    private var opener = ""

    override fun call(): Int {
        val entry = use(Hay::class).getEntry(relativePath)

        entry[HayOpener.EntryKey.OPENER] = opener
        entry.setProp(HayOpener.EntryKey.OPENER, opener)

        return ExitCode.OK
    }
}