package burrow.kernel.furnishing

import java.nio.file.Path
import java.util.*

/**
 * A carton is represented by a path in the filesystem, a bunch of properties,
 * and a set of furnishing classes.
 */
class Carton(val path: Path) {
    val properties = Properties()
    val furnishingClasses = mutableSetOf<FurnishingClass>()
}