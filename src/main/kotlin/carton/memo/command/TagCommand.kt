package burrow.carton.memo.command

import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "tag",
    header = ["Displays all tags."]
)
class TagCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        return ExitCode.OK
    }
}