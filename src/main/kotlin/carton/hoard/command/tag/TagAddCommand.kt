package burrow.carton.hoard.command.tag

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.HoardTag
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "tag.add",
    header = ["Adds a tag to an entry."]
)
class TagAddCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The entry to add the tag."],
    )
    private var id = 0

    @Parameters(index = "1", description = ["The tag to add."])
    private var tag = ""

    override fun call(): Int {
        use(Hoard::class).storage.operate(id) {
            use(HoardTag::class).getTags(this).add(tag)
        }

        return ExitCode.OK
    }
}