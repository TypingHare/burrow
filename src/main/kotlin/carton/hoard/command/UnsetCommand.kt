package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.checkId
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "unset",
    header = ["Unsets properties."]
)
class UnsetCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = [
            "The unique ID of the entry to modify. Must be a positive integer."
        ]
    )
    var id = 0

    @Parameters(index = "1..*", description = ["The keys to unset."])
    var keys: Array<String> = emptyArray()

    override fun call(): Int {
        if (!checkId(id, stderr)) {
            return ExitCode.USAGE
        }

        use(Hoard::class).storage.operate(id) { unset(keys.toList()) }
        return dispatch(EntryCommand::class, listOf(id.toString()))
    }
}