package burrow.kernel.path

import java.nio.file.Path

/**
 * Represents an interface for objects that are bound to a specific file system
 * path.
 */
interface PathBound {
    /**
     * Retrieves the path associated with this object.
     */
    fun getPath(): Path
}