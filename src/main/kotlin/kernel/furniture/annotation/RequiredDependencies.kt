package burrow.kernel.furniture.annotation

/**
 * Represents a bunch of required dependencies.
 */
annotation class RequiredDependencies(
    vararg val dependencies: Dependency
)