package burrow.kernel.furnishing

import burrow.kernel.furnishing.annotation.Furniture
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.net.URL
import java.net.URLClassLoader

class FurnishingWareHouse {
    private val classLoaderInfoMap = mutableMapOf<ClassLoader, Info>()
    val furnishingClasses = mutableSetOf<FurnishingClass>()

    companion object {
        @Throws(InvalidFurnishingClassException::class)
        fun checkFurnishingClass(furnishingClass: FurnishingClass) {
            furnishingClass.java.getAnnotation(Furniture::class.java)
                ?: throw InvalidFurnishingClassException(
                    furnishingClass.java.name,
                    "The furniture class is not annotated by " +
                            Furniture::class.java.name
                )
        }
    }

    fun getFurnishingClass(id: String): FurnishingClass? {
        return furnishingClasses.firstOrNull { it.java.name == id }
    }

    fun scanPackage(
        classLoader: ClassLoader,
        packageNames: Set<String>
    ): Info {
        val filterBuilder = FilterBuilder().apply {
            packageNames.forEach { includePackage(it) }
        }

        val urlCollection: Collection<URL> =
            if (classLoader is URLClassLoader) {
                classLoader.urLs.toList()
            } else {
                packageNames.flatMap {
                    ClasspathHelper.forPackage(it, classLoader)
                }.toList()
            }

        val configuration = ConfigurationBuilder()
            .filterInputsBy(filterBuilder)
            .setUrls(urlCollection)
            .addClassLoaders(classLoader)

        val reflections = Reflections(configuration)
        val furnishingClasses =
            reflections.getSubTypesOf(Furnishing::class.java)
                .onEach { checkFurnishingClass(it.kotlin) }

        val kotlinFurnishingClasses =
            furnishingClasses.map { it.kotlin }.toSet()
        this.furnishingClasses.addAll(kotlinFurnishingClasses)
        return Info(packageNames.toSet(), kotlinFurnishingClasses).apply {
            classLoaderInfoMap[classLoader] = this
        }
    }

    data class Info(
        val packageNames: Set<String>,
        val furnishingClasses: Set<FurnishingClass>,
    )
}

class InvalidFurnishingClassException(name: String, info: String) :
    RuntimeException("Invalid Furnishing class: $name ($info)")
