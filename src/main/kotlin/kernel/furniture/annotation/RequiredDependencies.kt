package burrow.kernel.furniture.annotation

/**
 * Represents a collection of required dependencies.
 */
annotation class RequiredDependencies(
    vararg val dependencies: Dependency
)