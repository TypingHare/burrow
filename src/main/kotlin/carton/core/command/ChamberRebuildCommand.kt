package burrow.carton.core.command

import burrow.carton.core.Core
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "chamber.rebuild",
    description = ["Rebuilds this chamber."]
)
class ChamberRebuildCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(Core::class).rebuildChamberPreservingConfig(stderr)
        return ExitCode.OK
    }
}