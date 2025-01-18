package burrow.carton.memo.command

import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "search",
    header = ["Search for a key."]
)
class SearchCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        // TODO: Use fuzzy search to implement this, but how?
        return ExitCode.OK
    }
}