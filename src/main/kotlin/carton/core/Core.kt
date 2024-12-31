package burrow.carton.core

import burrow.carton.core.command.ChamberDestroyCommand
import burrow.carton.core.command.ChamberRebuildCommand
import burrow.carton.core.command.FurnishingCommand
import burrow.carton.core.command.RootCommand
import burrow.kernel.Burrow
import burrow.kernel.chamber.ChamberPostBuildEvent
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.config.Config
import burrow.kernel.furniture.*
import burrow.kernel.furniture.annotation.Furniture
import java.io.PrintWriter

@Furniture(
    version = Burrow.VERSION,
    description = "The core furnishing.",
    type = Furniture.Type.COMPONENT
)
class Core(renovator: Renovator) : Furnishing(renovator) {
    /**
     * Mapping from chamber name to the original config map.
     */
    private val originalConfigMap = mutableMapOf<String, Config>()

    override fun assemble() {
        // Basic commands
        registerCommand(RootCommand::class)
        registerCommand(ChamberRebuildCommand::class)
        registerCommand(ChamberDestroyCommand::class)

        // Furnishing commands
        registerCommand(FurnishingCommand::class)

        burrow.courier.subscribe(ChamberPostBuildEvent::class) {
            originalConfigMap[it.chamber.name] = chamber.config.clone()
        }

        burrow.courier.unsubscribe(ChamberPostBuildEvent::class) {
            originalConfigMap.remove(it.chamber.name)
        }
    }

    @Throws(Exception::class)
    fun rebuildChamberPreservingConfig(stderr: PrintWriter) {
        try {
            chamberShepherd.rebuildChamber(chamber.name)
        } catch (ex: Exception) {
            stderr.println(ex.message)
            stderr.println("Error during restarting. Now roll back to the original config.")

            val originalConfig = originalConfigMap[chamber.name]!!
            config.entries.putAll(originalConfig.entries)
            config.isModified.set(originalConfig.isModified.get())

            throw ex
        }
    }

    fun getFurnishingClasses(): List<FurnishingClass> =
        mutableSetOf<FurnishingClass>()
            .apply { renovator.depTree.resolve { add(it::class) } }
            .sortedBy { extractId(it) }
            .toList()

    fun getAvailableFurnishingClasses(): List<FurnishingClass> {
        val isRoot = chamber.name == ChamberShepherd.ROOT_CHAMBER_NAME
        return burrow.warehouse.furnishingClasses
            .filter { isRoot || extractType(it) != Furniture.Type.ROOT }
            .sortedBy { extractId(it) }
            .toList()
    }
}