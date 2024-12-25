package burrow.kernel.furnishing

import burrow.kernel.chamber.ExtendedChamberModule
import burrow.kernel.command.CommandClass
import burrow.kernel.config.ConfigLifeCycle
import burrow.kernel.furnishing.annotation.Dependency
import burrow.kernel.furnishing.annotation.RequiredDependencies
import kotlin.reflect.KClass

abstract class Furnishing(renovator: Renovator) :
    ExtendedChamberModule(renovator.chamber), ConfigLifeCycle,
    FurnishingProvider, FurnishingLifeCycle {
    val commandClasses = mutableSetOf<CommandClass>()

    fun getDependencies(): List<Dependency> {
        val dependsOnAnnotation =
            this::class.java.getAnnotation(RequiredDependencies::class.java)

        return dependsOnAnnotation?.dependencies?.toList() ?: emptyList()
    }

    @Throws(FurnishingNotFoundException::class)
    override fun <F : Furnishing> use(furnishingClass: KClass<F>): F =
        renovator.getFurnishing(furnishingClass)

    fun registerCommand(commandClass: CommandClass) {
        commandClasses.add(commandClass)
        processor.registerCommand(commandClass)
    }
}

fun getId(furnishing: Furnishing): String = furnishing::class.java.name

typealias FurnishingClass = KClass<out Furnishing>