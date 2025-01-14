package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.checkId
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "prop",
    header = ["Retrieve the value of a property."]
)
class PropCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = [
            "The unique ID of the entry."
        ]
    )
    var id = 0

    @Parameters(
        index = "1",
        description = [
            "The key that associated with the value to retrieve."
        ]
    )
    var key = ""

    @Option(
        names = ["--left", "-l"],
        description = ["Retrieve raw property."]
    )
    var shouldRetrieveLeftValue = false

    override fun call(): Int {
        if (!checkId(id, stderr)) return ExitCode.USAGE
        if (key.isEmpty()) return ExitCode.OK

        val entry = use(Hoard::class).storage[id]
        if (entry.containsKey(key)) {
            when (shouldRetrieveLeftValue) {
                true -> stdout.println(entry.getLeft(key))
                false -> stdout.println(entry.get<Any>(key)?.toString())
            }
        }

        return ExitCode.OK
    }
}