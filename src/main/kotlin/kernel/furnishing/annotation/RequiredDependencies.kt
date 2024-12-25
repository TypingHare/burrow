package burrow.kernel.furnishing.annotation

// Represents a bunch of required dependencies
annotation class RequiredDependencies(
    vararg val dependencies: Dependency
)
