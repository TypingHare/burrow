package burrow.carton.hoard.command.tag

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.HoardTag
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "tags",
    header = ["Display tags of an entry."]
)
class TagsCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The entry to add the tag."],
    )
    private var id = 0

    override fun call(): Int {
        use(Hoard::class).storage.operate(id) {
            use(HoardTag::class).getTags(this).forEach(stdout::println)
        }

        return ExitCode.OK
    }
}