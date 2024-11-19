package burrow.carton.clutter

import burrow.kernel.furnishing.FurnishingWareHouse
import java.net.URLClassLoader
import java.util.*

data class Carton(
    val classLoader: URLClassLoader,
    val properties: Properties,
    val info: FurnishingWareHouse.Info,
)
