package burrow.kernel.furnishing

import kotlin.reflect.KClass

annotation class DependsOn(val dependencies: Array<KClass<out Furnishing>>)
