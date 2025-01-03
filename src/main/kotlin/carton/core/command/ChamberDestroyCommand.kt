package burrow.carton.core.command

import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "chamber.destroy",
    header = ["Destroys this chamber."]
)
class ChamberDestroyCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        chamberShepherd.destroyChamber(chamber.name)
        return ExitCode.OK
    }
}