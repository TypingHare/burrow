package burrow.kernel.furniture

import burrow.common.converter.StringConverterPair
import burrow.kernel.chamber.ExtendedChamberModule
import burrow.kernel.config.ConfigLifeCycle
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies
import burrow.kernel.terminal.CommandClass
import burrow.kernel.terminal.CommandRegistry
import kotlin.reflect.KClass

abstract class Furnishing(renovator: Renovator) :
    ExtendedChamberModule(renovator.chamber),
    CommandRegistry,
    ConfigLifeCycle,
    FurnishingLifeCycle,
    FurnishingProvider {

    val commandClasses = mutableSetOf<CommandClass>()
    val configKeys = mutableSetOf<String>()

    /**
     * Retrieves the dependencies this furnishing requires.
     */
    fun getDependencies(): List<Dependency> = extractDependencies(this::class)

    override fun registerCommand(commandClass: CommandClass) {
        commandClasses.add(commandClass)
        interpreter.registerCommand(commandClass)
    }

    fun registerConfigKey(
        key: String,
        @Suppress("UNCHECKED_CAST") converterPair: StringConverterPair<*> =
            StringConverterPair.IDENTITY as StringConverterPair<Any>
    ) {
        @Suppress("UNCHECKED_CAST")
        config.converterPairContainer.add(
            key,
            converterPair as StringConverterPair<Any>
        )
        configKeys.add(key)
    }

    override fun unregisterCommand(commandClass: CommandClass) {
        commandClasses.remove(commandClass)
        interpreter.unregisterCommand(commandClass)
    }

    override fun <F : Furnishing> use(furnishingClass: KClass<F>): F =
        renovator.getFurnishing(furnishingClass)
            ?: throw NotDependencyFurnishingException(furnishingClass.java.name)
}

typealias FurnishingClass = KClass<out Furnishing>

fun extractFurniture(furnishingClass: FurnishingClass): Furniture? =
    furnishingClass.java.getAnnotation(Furniture::class.java)

fun extractId(furnishingClass: FurnishingClass): String =
    furnishingClass.java.name

fun extractVersion(furnishingClass: FurnishingClass): String =
    extractFurniture(furnishingClass)!!.version

fun extractDescription(furnishingClass: FurnishingClass): String =
    extractFurniture(furnishingClass)!!.description

fun extractType(furnishingClass: FurnishingClass): String =
    extractFurniture(furnishingClass)!!.type

fun extractDependencies(furnishingClass: FurnishingClass): List<Dependency> =
    furnishingClass.java
        .getAnnotation(RequiredDependencies::class.java)
        ?.dependencies
        ?.toList() ?: emptyList()

class NotDependencyFurnishingException(id: String) :
    RuntimeException("Not a dependency furnishing: $id")