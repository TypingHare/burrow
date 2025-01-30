package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.checkPairs
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "set.all",
    header = ["Sets properties for all the entries in the hoard."]
)
class SetAllCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "1..*",
        description = [
            "The keys and values to set as properties for all entries."
        ]
    )
    private var pairs: Array<String> = emptyArray()

    override fun call(): Int {
        if (!checkPairs(pairs, stderr)) {
            return ExitCode.USAGE
        }

        val properties = mutableMapOf<String, String>()
        for (i in pairs.indices step 2) {
            properties[pairs[i]] = pairs[i + 1]
        }

        val hoard = use(Hoard::class)
        hoard.storage.getAllEntries().forEach { it.set(properties) }

        return ExitCode.OK
    }
}