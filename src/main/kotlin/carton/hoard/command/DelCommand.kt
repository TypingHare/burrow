package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.checkId
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "del",
    header = ["Deletes entries."]
)
class DelCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0..*",
        description = [
            "The unique ID of the entry to modify. Must be a positive integer."
        ]
    )
    private var idArray: Array<Int> = emptyArray()

    @Option(
        names = ["--quiet", "-q"]
    )
    private var shouldBeQuiet: Boolean = false

    override fun call(): Int {
        idArray.forEach(this::deleteEntryById)
        return ExitCode.OK
    }

    private fun deleteEntryById(id: Int) {
        if (!checkId(id, stderr, shouldBeQuiet)) {
            return
        }

        val hoard = use(Hoard::class)
        if (hoard.storage.exists(id)) {
            if (!shouldBeQuiet) {
                dispatch(EntryCommand::class, listOf(id.toString()))
            }

            hoard.storage.delete(id)
        }
    }
}