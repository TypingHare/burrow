package burrow.kernel.furnishing.annotation

import burrow.kernel.furnishing.FurnishingClass

/**
 * A dependency is represented as a target furnishing class and a version
 * control string.
 */
annotation class Dependency(
    val target: FurnishingClass,
    val version: String
)