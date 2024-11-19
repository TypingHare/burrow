package burrow.carton.hoard

import burrow.kernel.chamber.Chamber
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.annotation.Furniture

@Furniture(
    version = "0.0.0",
    description = "Creation time.",
    type = Furniture.Type.COMPONENT
)
class HoardTime(chamber: Chamber) : Furnishing(chamber) {
    override fun assemble() {
    }
}