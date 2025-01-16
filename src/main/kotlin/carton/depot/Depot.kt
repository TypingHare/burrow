package burrow.carton.depot

import burrow.carton.cradle.Cradle
import burrow.carton.haystack.HaystackOpener
import burrow.carton.inverse.Inverse
import burrow.carton.inverse.annotation.InverseRegisterCommands
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
    Dependency(Inverse::class, Burrow.VERSION),
    Dependency(HaystackOpener::class, Burrow.VERSION),
    Dependency(Cradle::class, Burrow.VERSION)
)
@InverseRegisterCommands
class Depot(renovator: Renovator) : Furnishing(renovator)