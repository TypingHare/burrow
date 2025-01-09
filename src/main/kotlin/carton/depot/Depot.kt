package burrow.carton.depot

import burrow.carton.cradle.Cradle
import burrow.carton.depot.command.ListCommand
import burrow.carton.haystack.HaystackOpener
import burrow.carton.haystack.command.*
import burrow.carton.haystack.command.opener.InfoCommand
import burrow.carton.haystack.command.opener.OpenCommand
import burrow.carton.haystack.command.opener.OpenerSetCommand
import burrow.kernel.Burrow
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies

@Furniture(
    version = Burrow.VERSION,
    description = "Manages git repositories easily.",
    type = Furniture.Type.MAIN
)
@RequiredDependencies(
    Dependency(HaystackOpener::class, Burrow.VERSION),
    Dependency(Cradle::class, Burrow.VERSION)
)
class Depot(renovator: Renovator) : Furnishing(renovator) {
    override fun assemble() {
        // Essential commands from Haystack
        registerCommand(PathListCommand::class)
        registerCommand(PathAddCommand::class)
        registerCommand(PathRemoveCommand::class)
        registerCommand(NewCommand::class)
        registerCommand(InfoCommand::class)
        registerCommand(OpenerSetCommand::class)
        registerCommand(OpenCommand::class)
        registerCommand(ScanCommand::class)

        registerCommand(ListCommand::class)
    }
}