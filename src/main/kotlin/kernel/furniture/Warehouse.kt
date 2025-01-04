package burrow.kernel.furniture

import burrow.kernel.furniture.annotation.Furniture
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import org.slf4j.LoggerFactory
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*

class Warehouse {
    val cartons = mutableMapOf<ClassLoader, Carton>()
    val furnishingClasses = mutableSetOf<FurnishingClass>()

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
        val filterBuilder = FilterBuilder().apply {
            packageNames.forEach { includePackage(it) }
        }

        val urlCollection: List<URL> = when (classLoader) {
            is URLClassLoader -> classLoader.urLs.toList()
            else -> packageNames
                .flatMap { ClasspathHelper.forPackage(it, classLoader) }
                .toList()
        }

        val configuration = ConfigurationBuilder()
            .filterInputsBy(filterBuilder)
            .setUrls(urlCollection)
            .addClassLoaders(classLoader)

        val reflections = Reflections(configuration)
        val d = reflections.getSubTypesOf(Furnishing::class.java)
        val furnishingClasses = reflections
            .getSubTypesOf(Furnishing::class.java)
            .onEach { checkFurnishingClass(it.kotlin) }

        val kotlinFurnishingClasses = furnishingClasses
            .map { it.kotlin }
            .toSet()
        this.furnishingClasses.addAll(kotlinFurnishingClasses)

        kotlinFurnishingClasses.forEach {
            logger.debug("Added furnishing class: ${it.java.name}")
        }

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

        private val logger = LoggerFactory.getLogger(Warehouse::class.java)
    }
}

class InvalidFurnishingClassException(id: String, cause: Exception) :
    RuntimeException("Invalid Furnishing class: $id", cause)

class NotAnnotatedByFurnitureException(id: String) :
    RuntimeException("Furnishing class should annotated by ${Furniture::class.java.name} : $id")