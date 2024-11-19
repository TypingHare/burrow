package burrow.kernel.furnishing

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ExtendedChamberModule
import burrow.kernel.command.CommandClass
import burrow.kernel.config.ConfigSupport
import burrow.kernel.furnishing.annotation.DependsOn
import burrow.kernel.furnishing.annotation.Furniture
import kotlin.reflect.KClass

abstract class Furnishing(chamber: Chamber) : ExtendedChamberModule(chamber),
    ConfigSupport {
    companion object {
        private fun getFurnitureAnnotation(furnishingClass: FurnishingClass): Furniture =
            furnishingClass.java.getAnnotation(Furniture::class.java)

        fun extractId(furnishingClass: FurnishingClass): String =
            furnishingClass.java.name

        fun extractLabel(furnishingClass: FurnishingClass): String {
            return getFurnitureAnnotation(furnishingClass).label
                .takeIf { it.isNotBlank() }
                ?: furnishingClass.java.simpleName
        }

        fun extractVersion(furnishingClass: FurnishingClass): String {
            return getFurnitureAnnotation(furnishingClass).version
        }

        fun extractDescription(furnishingClass: FurnishingClass): String {
            return getFurnitureAnnotation(furnishingClass).description
        }

        fun extractType(furnishingClass: FurnishingClass): String {
            return getFurnitureAnnotation(furnishingClass).type
        }

        fun extractDependencies(furnishingClass: FurnishingClass): List<FurnishingClass> {
            val dependsOn =
                furnishingClass.java.getAnnotation(DependsOn::class.java)
            return dependsOn?.dependencies?.toList() ?: emptyList()
        }
    }

    val commands = mutableSetOf<CommandClass>()

    fun getId(): String = extractId(this::class)

    fun getLabel(): String = extractLabel(this::class)

    fun getDependencies(): List<String> {
        val dependsOnAnnotation =
            this::class.java.getAnnotation(DependsOn::class.java)

        return dependsOnAnnotation?.dependencies?.map { it.java.name }
            .orEmpty()
    }

    protected fun registerCommand(commandClass: CommandClass) {
        commands.add(commandClass)
        processor.registerCommand(commandClass)
    }

    protected fun <F : Furnishing> use(furnishingClass: KClass<F>): F {
        return renovator.getFurnishing(furnishingClass)
            ?: throw NotDependencyFurnishingException(furnishingClass.java.name)
    }

    /**
     * Registers commands and subscribes to events.
     */
    open fun assemble() {}

    /**
     * Initializes the chamber by reading configurations.
     */
    open fun launch() {}

    open fun discard() {}
}

typealias FurnishingClass = KClass<out Furnishing>

class NotDependencyFurnishingException(id: String) :
    RuntimeException("Not a dependency furnishing: $id")