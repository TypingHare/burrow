package burrow.furnishing.dictator

import burrow.furnishing.standard.Standard
import burrow.kernel.chamber.Chamber
import burrow.kernel.furnishing.DependsOn
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.Furniture

@DependsOn([Standard::class])
@Furniture(
    version = "0.0.0",
    description = "Dictator allows developers to manage chambers.",
    type = Furniture.Type.ROOT
)
class Dictator(chamber: Chamber) : Furnishing(chamber) {
    override fun assemble() {
        registerCommand(ChambersCommand::class)
    }
}