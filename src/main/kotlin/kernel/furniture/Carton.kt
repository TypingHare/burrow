package burrow.kernel.furniture

import java.nio.file.Path
import java.util.*

/**
 * A carton is represented by a file system path, a bunch of properties,
 * and a set of furnishing classes.
 */
class Carton(val path: Path, val classLoader: ClassLoader) {
    val properties = Properties()
    val furnishingClasses = mutableSetOf<FurnishingClass>()
}