package burrow.carton.git

import burrow.carton.cradle.Cradle
import burrow.carton.git.command.InfoCommand
import burrow.carton.git.command.NewCommand
import burrow.carton.git.command.OpenCommand
import burrow.carton.hay.Hay
import burrow.carton.hay.command.PathAddCommand
import burrow.carton.hay.command.PathCommand
import burrow.carton.hay.command.PathRemoveCommand
import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.annotation.DependsOn
import burrow.kernel.furnishing.annotation.Furniture

@Furniture(
    version = Burrow.VERSION.NAME,
    description = "Manage git repositories easily.",
    type = Furniture.Type.MAIN
)
@DependsOn(Hay::class, Cradle::class)
class Git(chamber: Chamber) : Furnishing(chamber) {
    override fun assemble() {
        // Essential commands from Hay
        registerCommand(PathCommand::class)
        registerCommand(PathAddCommand::class)
        registerCommand(PathRemoveCommand::class)

        registerCommand(NewCommand::class)
        registerCommand(InfoCommand::class)
        registerCommand(OpenCommand::class)
    }
}