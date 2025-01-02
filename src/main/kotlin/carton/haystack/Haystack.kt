package burrow.carton.haystack

import burrow.kernel.Burrow
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Furniture

@Furniture(
    version = Burrow.VERSION,
    description = "Manages pairs of relative paths and absolute paths.",
    type = Furniture.Type.COMPONENT
)
class Haystack(renovator: Renovator) : Furnishing(renovator) {
}