package burrow.carton.core.command.chamber

import burrow.carton.core.Core
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "chamber.rebuild",
    header = ["Rebuilds this chamber; rolls back if the rebuilding fails."]
)
class ChamberRebuildCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        if (chamber.name == ChamberShepherd.ROOT_CHAMBER_NAME) {
            stdout.println("Rebuilding the root chamber is forbidden.")
            return ExitCode.USAGE
        }

        use(Core::class).rebuildChamber(stderr)
        return ExitCode.OK
    }
}