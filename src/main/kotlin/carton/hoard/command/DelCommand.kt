package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.checkId
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "del",
    header = ["Deletes an entry."]
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
        if (!hoard.storage.exists(id)) {
            stderr.println("Entry with such ID doesn't exist: $id")
            return ExitCode.USAGE
        }

        dispatch(EntryCommand::class, listOf(id.toString()))
        hoard.storage.delete(id)

        return ExitCode.OK
    }
}