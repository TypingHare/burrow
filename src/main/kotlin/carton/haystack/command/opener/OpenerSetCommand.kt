package burrow.carton.haystack.command.opener

import burrow.carton.haystack.Haystack
import burrow.carton.haystack.HaystackOpener
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "opener.set",
    header = ["Sets the opener."]
)
class OpenerSetCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The relative path of the entry."]
    )
    private var relativePath = ""

    @Parameters(
        index = "1",
        description = ["The opener to use."]
    )
    private var opener = ""

    override fun call(): Int {
        val entry = use(Haystack::class).getEntry(relativePath)
        entry[HaystackOpener.EntryKey.OPENER] = opener

        return ExitCode.OK
    }
}