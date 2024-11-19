package burrow.carton.bin

import burrow.kernel.chamber.Chamber
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.annotation.Furniture

@Furniture(
    version = "0.0.0",
    description = "Bin",
    type = Furniture.Type.COMPONENT
)
class Bin(chamber: Chamber) : Furnishing(chamber)