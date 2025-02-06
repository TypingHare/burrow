package burrow.carton.haystack.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.HoardPair
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "del",
    header = ["Deletes an entry."]
)
class DelCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0..*",
        description = ["The IDs or names of the entries to delete."]
    )
    private var idOrNameArray: Array<String> = emptyArray()

    private val hoardPair = use(HoardPair::class)

    private val hoard = use(Hoard::class)

    override fun call(): Int {
        idOrNameArray.forEach { deleteByIdOrName(it) }
        return ExitCode.OK
    }

    private fun deleteByIdOrName(idOrName: String) {
        val id = idOrName.toIntOrNull() ?: hoardPair.getFirstEntryOrThrow(
            idOrName
        ).id
        hoard.storage.delete(id)
    }
}