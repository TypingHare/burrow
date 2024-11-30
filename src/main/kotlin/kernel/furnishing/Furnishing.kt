package burrow.kernel.furnishing

import burrow.kernel.chamber.ExtendedChamberModule
import burrow.kernel.command.CommandClass
import burrow.kernel.config.ConfigSupport
import burrow.kernel.furnishing.annotation.Furniture
import burrow.kernel.furnishing.annotation.RequiredDependencies
import kotlin.reflect.KClass

abstract class Furnishing(renovator: Renovator) :
    ExtendedChamberModule(renovator.chamber), ConfigSupport,
    FurnishingProvider {
    val commands = mutableSetOf<CommandClass>()

    fun getDependencies(): List<String> {
        val dependsOnAnnotation =
            this::class.java.getAnnotation(RequiredDependencies::class.java)

        return dependsOnAnnotation?.dependencies?.map { it.java.name }
            .orEmpty()
    }

    @Throws(FurnishingNotFoundException::class)
    override fun <F : Furnishing> use(furnishingClass: KClass<F>): F =
        renovator.getFurnishing(furnishingClass)

    fun registerCommand(commandClass: CommandClass) {
        commands.add(commandClass)
        processor.registerCommand(commandClass)
    }

    open fun assemble() = Unit

    open fun launch() = Unit

    companion object {
        private fun getFurnitureAnnotation(furnishingClass: FurnishingClass): Furniture =
            furnishingClass.java.getAnnotation(Furniture::class.java)

        fun extractId(furnishingClass: FurnishingClass): String =
            furnishingClass.java.name

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
                furnishingClass.java.getAnnotation(RequiredDependencies::class.java)
            return dependsOn?.dependencies?.toList() ?: emptyList()
        }
    }
}

typealias FurnishingClass = KClass<out Furnishing>