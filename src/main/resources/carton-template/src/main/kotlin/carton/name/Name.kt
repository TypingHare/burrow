package burrow.carton.{{carton_name}}

import burrow.carton.inverse.Inverse
import burrow.carton.inverse.annotation.InverseRegisterCommands
import burrow.carton.inverse.annotation.InverseSetConfig
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies

const val VERSION = "0.0.0"
const val REQUIRED_BURROW_VERSION = "0.0.0"

@Furniture(
    version = VERSION,
    description = "A carton template.",
    type = Furniture.Type.MAIN
)
@RequiredDependencies(
    Dependency(Inverse::class, REQUIRED_BURROW_VERSION),
)
@InverseSetConfig
@InverseRegisterCommands
class {{furnishing_class_name}}(renovator: Renovator) : Furnishing(renovator)