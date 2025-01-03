package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "exist",
    header = ["Checks if an entry exists."]
)
class ExistCommand(data: CommandData) : Command(data) {
    @Parameters(index = "0", description = ["The ID of the entry."])
    private var id: Int = 0

    override fun call(): Int {
        if (id <= 0) {
            stderr.println(
                "Error: Entry ID must be a positive integer. Provided ID: $id"
            )
            return ExitCode.USAGE
        }

        val isExist = use(Hoard::class).exists(id)
        stdout.println(if (isExist) "true" else "false")

        return ExitCode.OK
    }
}