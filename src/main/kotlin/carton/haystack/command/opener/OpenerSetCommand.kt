package burrow.carton.haystack.command.opener

import burrow.carton.haystack.Haystack
import burrow.carton.haystack.HaystackOpener
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "opener.set",
    header = ["Sets the opener for an entry."]
)
class OpenerSetCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The name of the entry."]
    )
    private var name = ""

    @Parameters(
        index = "1",
        description = ["The opener to set."]
    )
    private var opener = ""

    override fun call(): Int {
        val entry = use(Haystack::class).getEntry(name)
        entry[HaystackOpener.EntryKey.OPENER] = opener

        return ExitCode.OK
    }
}