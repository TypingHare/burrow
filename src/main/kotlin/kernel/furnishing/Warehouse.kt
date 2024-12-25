package burrow.kernel.furnishing

import burrow.kernel.furnishing.annotation.Furniture
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*
import kotlin.Throws

class Warehouse {
    private val cartons = mutableMapOf<ClassLoader, Carton>()
    private val furnishingClasses = mutableSetOf<FurnishingClass>()

    fun getFurnishingClass(id: String): FurnishingClass? =
        furnishingClasses.firstOrNull { it.java.name == id }

    fun scanPackage(
        path: Path,
        classLoader: ClassLoader,
        properties: Properties,
    ): Carton {
        val packageNames = properties
            .getProperty(PROPERTY_PACKAGES)
            .split(PACKAGE_DELIMITER)
            .filterNotNull()
        val filterBuilder = FilterBuilder().apply {
            packageNames.forEach { includePackage(it) }
        }

        val urlCollection: List<URL> = when (classLoader) {
            is URLClassLoader -> classLoader.urLs.toList()
            else -> packageNames.flatMap {
                ClasspathHelper.forPackage(it, classLoader)
            }.toList()
        }

        val configuration = ConfigurationBuilder()
            .filterInputsBy(filterBuilder)
            .setUrls(urlCollection)
            .addClassLoaders(classLoader)

        val reflections = Reflections(configuration)
        val furnishingClasses = reflections
            .getSubTypesOf(Furnishing::class.java)
            .onEach { checkFurnishingClass(it.kotlin) }

        val kotlinFurnishingClasses = furnishingClasses
            .map { it.kotlin }
            .toSet()
        this.furnishingClasses.addAll(kotlinFurnishingClasses)

        return Carton(path).apply {
            this.properties.putAll(properties)
            this.furnishingClasses.addAll(kotlinFurnishingClasses)
            cartons[classLoader] = this
        }
    }

    companion object {
        const val PROPERTY_PACKAGES = "burrow.packages"

        const val PACKAGE_DELIMITER = ":"

        @Throws(InvalidFurnishingClassException::class)
        fun checkFurnishingClass(furnishingClass: FurnishingClass) {
            val furnishingId = furnishingClass.java.name
            try {
                furnishingClass.java.getAnnotation(Furniture::class.java)
                    ?: throw NotAnnotatedByFurnitureException(furnishingId)
            } catch (ex: Exception) {
                throw InvalidFurnishingClassException(furnishingId, ex)
            }
        }
    }
}

class InvalidFurnishingClassException(id: String, cause: Exception) :
    RuntimeException("Invalid Furnishing class: $id", cause)

class NotAnnotatedByFurnitureException(id: String) :
    RuntimeException("Furnishing class should annotated by ${Furniture::class.java.name} : $id")