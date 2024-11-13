package burrow.kernel.furnishing

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import burrow.kernel.command.CommandClass
import burrow.kernel.config.ConfigSupport
import kotlin.reflect.KClass

abstract class Furnishing(chamber: Chamber) : ChamberModule(chamber),
    ConfigSupport {
    companion object {
        fun extractId(furnishingClass: FurnishingClass): String =
            furnishingClass.java.name

        fun extractLabel(furnishingClass: FurnishingClass): String {
            val furnitureAnnotation =
                furnishingClass.java.getAnnotation(Furniture::class.java)

            return furnitureAnnotation?.label.takeIf { !it.isNullOrBlank() }
                ?: furnishingClass.java.simpleName
        }
    }

    private val commands = mutableSetOf<CommandClass>()

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

    open fun assemble() {}

    open fun launch() {}

    open fun discard() {}
}

typealias FurnishingClass = KClass<out Furnishing>