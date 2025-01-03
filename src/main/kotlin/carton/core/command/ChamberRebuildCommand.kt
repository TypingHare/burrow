package burrow.carton.core.command

import burrow.carton.core.Core
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "chamber.rebuild",
    header = ["Rebuilds this chamber."]
)
class ChamberRebuildCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(Core::class).rebuildChamber(stderr)
        return ExitCode.OK
    }
}