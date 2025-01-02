package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.checkId
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "del",
    description = ["Deletes an entry."]
)
class DelCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = [
            "The unique ID of the entry to modify. Must be a positive integer."
        ]
    )
    var id = 0

    override fun call(): Int {
        if (!checkId(id, stderr)) {
            return ExitCode.USAGE
        }

        val hoard = use(Hoard::class)
        if (!hoard.exists(id)) {
            stderr.println("Entry with such ID doesn't exist: $id")
            return ExitCode.USAGE
        }

        dispatch(EntryCommand::class, listOf(id.toString()))
        hoard.delete(id)

        return ExitCode.OK
    }
}