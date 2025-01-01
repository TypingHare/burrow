package burrow.carton.dictator

import burrow.carton.core.Core
import burrow.kernel.Burrow
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies

@Furniture(
    version = Burrow.VERSION,
    description = "Allows developers to manage chambers.",
    type = Furniture.Type.ROOT
)
@RequiredDependencies(Dependency(Core::class, Burrow.VERSION))
class Dictator(renovator: Renovator) : Furnishing(renovator)