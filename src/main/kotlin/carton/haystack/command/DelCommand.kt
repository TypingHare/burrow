package burrow.carton.haystack.command

import burrow.carton.haystack.Haystack
import burrow.carton.hoard.Hoard
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "del",
    header = ["Deletes an entry."]
)
class DelCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The name of the entry to delete."]
    )
    private var name = ""

    override fun call(): Int {
        val entry = use(Haystack::class).getEntry(name)
        use(Hoard::class).storage.delete(entry.id)

        return ExitCode.OK
    }
}