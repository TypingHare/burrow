package burrow.kernel.furniture.annotation

import burrow.kernel.furniture.FurnishingClass

/**
 * A dependency is represented as a target furnishing class and a version
 * control string.
 */
annotation class Dependency(
    val target: FurnishingClass,
    val version: String
)