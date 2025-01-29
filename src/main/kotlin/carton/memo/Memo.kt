package burrow.carton.memo

import burrow.carton.hoard.HoardPair
import burrow.carton.hoard.HoardTag
import burrow.carton.inverse.Inverse
import burrow.carton.inverse.annotation.InverseRegisterCommands
import burrow.carton.shell.Shell
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
    Dependency(Shell::class, Burrow.VERSION),
    Dependency(HoardPair::class, Burrow.VERSION),
    Dependency(HoardTag::class, Burrow.VERSION)
)
@InverseRegisterCommands
class Memo(renovator: Renovator) : Furnishing(renovator) {
    override fun launch() {
        use(Shell::class).createShellFileIfNotExist()
    }
}