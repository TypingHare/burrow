package burrow.carton.hoard.command.tag

import burrow.carton.hoard.HoardTag
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "tag.list",
    header = ["Displays all tags."]
)
class TagListCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(HoardTag::class).tagSet.sorted().forEach(stdout::println)
        return ExitCode.OK
    }
}