package burrow.carton.hoard.command.tag

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.HoardTag
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "tag.del",
    header = ["Deletes a tag from an entry."]
)
class TagDelCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The entry to delete the tag."],
    )
    private var id = 0

    @Parameters(index = "1", description = ["The tag to delete."])
    private var tag = ""

    override fun call(): Int {
        use(Hoard::class).storage.operate(id) {
            use(HoardTag::class).getTags(this).remove(tag)
        }

        return ExitCode.OK
    }
}