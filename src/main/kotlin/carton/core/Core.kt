package burrow.carton.core

import burrow.carton.core.command.RootCommand
import burrow.carton.core.command.TestCommand
import burrow.carton.standard.command.HelpCommand
import burrow.kernel.Burrow
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.Renovator
import burrow.kernel.furnishing.annotation.Furniture

@Furniture(
    version = Burrow.VERSION,
    description = "The core furnishing",
    type = Furniture.Type.COMPONENT
)
class Core(renovator: Renovator) : Furnishing(renovator) {
    override fun assemble() {
        // Basic commands
        registerCommand(RootCommand::class)
        registerCommand(TestCommand::class)
        registerCommand(HelpCommand::class)
    }
}