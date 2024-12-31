package burrow.kernel.furniture

import kotlin.reflect.KClass

interface FurnishingProvider {
    /**
     * Uses a furnishing in the current chamber scope.
     */
    fun <F : Furnishing> use(furnishingClass: KClass<F>): F
}