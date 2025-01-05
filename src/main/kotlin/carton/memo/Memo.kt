package burrow.carton.memo

import burrow.carton.hoard.HoardPair
import burrow.carton.inverse.Inverse
import burrow.carton.inverse.annotation.InverseRegisterCommands
import burrow.carton.inverse.annotation.InverseSetConfig
import burrow.kernel.Burrow
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies

@Furniture(
    version = Burrow.VERSION,
    description = "Manages a bunch of key-value pairs.",
    type = Furniture.Type.MAIN
)
@RequiredDependencies(
    Dependency(Inverse::class, Burrow.VERSION),
    Dependency(HoardPair::class, Burrow.VERSION)
)
@InverseSetConfig
@InverseRegisterCommands
class Memo(renovator: Renovator) : Furnishing(renovator) {
    companion object {
        const val TAG_DELIMITER = "||"
    }

    object EntryKey {
        const val TAGS = "TAGS"
    }
}