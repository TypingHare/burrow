package burrow.kernel.furnishing.annotation

import burrow.kernel.furnishing.Furnishing
import kotlin.reflect.KClass

annotation class DependsOn(val dependencies: Array<KClass<out Furnishing>>)
