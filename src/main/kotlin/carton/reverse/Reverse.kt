package burrow.carton.reverse

import burrow.kernel.Burrow
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Furniture

@Furniture(
    version = Burrow.VERSION,
    description = "Allows developers to create furnishings more easily.",
    type = Furniture.Type.COMPONENT
)
class Reverse(renovator: Renovator) : Furnishing(renovator)