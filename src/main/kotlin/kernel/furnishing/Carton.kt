package burrow.kernel.furnishing

import java.nio.file.Path

class Carton(val path: Path) {
    val packageNames = mutableSetOf<String>()
    val furnishingClasses = mutableSetOf<FurnishingClass>()
}