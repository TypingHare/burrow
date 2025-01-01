package burrow.carton.core

import burrow.carton.core.command.*
import burrow.kernel.Burrow
import burrow.kernel.chamber.ChamberPostBuildEvent
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.config.Config
import burrow.kernel.furniture.*
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.stream.StateWriterController
import burrow.kernel.stream.state.OutputState
import burrow.kernel.terminal.CommandNotFoundEvent
import burrow.kernel.terminal.ExitCode
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
        registerCommand(DefaultCommand::class)
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

        burrow.courier.subscribe(CommandNotFoundEvent::class) {
            EventHandler.commandNotFoundEventHandler(it)
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

    object EventHandler {
        fun commandNotFoundEventHandler(event: CommandNotFoundEvent) {
            val commandName = event.commandName
            val outputStream = event.commandData.environment.outputStream
            StateWriterController(outputStream, OutputState.STDOUT).let {
                it.getPrintWriter(OutputState.STDERR)
                    .println("Command not found: $commandName")
                it.getPrintWriter(OutputState.EXIT_CODE)
                    .println(ExitCode.USAGE)
            }
        }
    }
}