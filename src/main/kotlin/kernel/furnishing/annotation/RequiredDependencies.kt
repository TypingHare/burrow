package burrow.kernel.furnishing.annotation

import burrow.kernel.furnishing.Furnishing
import kotlin.reflect.KClass

annotation class RequiredDependencies(
    vararg val dependencies: KClass<out Furnishing>
)
