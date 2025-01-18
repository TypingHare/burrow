package burrow.carton.core.command.chamber

import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "destroy",
    header = ["Destroys this chamber."]
)
class DestroyCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        chamberShepherd.destroyChamber(chamber.name)
        return ExitCode.OK
    }
}