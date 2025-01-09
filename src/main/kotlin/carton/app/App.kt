package burrow.carton.app

import burrow.carton.haystack.Haystack
import burrow.carton.haystack.HaystackOpener
import burrow.kernel.Burrow
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies

@Furniture(
    version = Burrow.VERSION,
    description = "Manages applications on macOS.",
    type = Furniture.Type.MAIN
)
@RequiredDependencies(
    Dependency(Haystack::class, Burrow.VERSION),
    Dependency(HaystackOpener::class, Burrow.VERSION),
)
class App(renovator: Renovator) : Furnishing(renovator) {
    init {
        checkSystem()
    }

    @Throws(NotMacOperatingSystemException::class)
    private fun checkSystem() {
        val os = System.getProperty("os.name").lowercase()
        if (os.contains("mac")) {
            throw NotMacOperatingSystemException()
        }
    }
}

class NotMacOperatingSystemException :
    RuntimeException("Your operating system is not macOS!")