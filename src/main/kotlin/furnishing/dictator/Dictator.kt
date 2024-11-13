package burrow.furnishing.dictator

import burrow.kernel.chamber.Chamber
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.Furniture

@Furniture(
    description = "Dictatorship",
    type = Furniture.Type.ROOT
)
class Dictator(chamber: Chamber) : Furnishing(chamber)