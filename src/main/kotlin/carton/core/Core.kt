package burrow.carton.core

import burrow.carton.core.command.*
import burrow.carton.core.command.config.ConfigCommand
import burrow.carton.core.command.config.ConfigGetCommand
import burrow.carton.core.command.config.ConfigSetCommand
import burrow.carton.core.command.furnishing.FurnishingCommand
import burrow.kernel.Burrow
import burrow.kernel.chamber.ChamberPostBuildEvent
import burrow.kernel.chamber.ChamberPostDestroyEvent
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.config.Config
import burrow.kernel.furniture.*
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.stream.StateWriterController
import burrow.kernel.stream.state.OutputState
import burrow.kernel.terminal.CommandClass
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

    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.DESCRIPTION)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.DESCRIPTION, "")
    }

    override fun assemble() {
        // Basic commands
        registerCommand(DefaultCommand::class)
        registerCommand(RootCommand::class)
        registerCommand(ChamberRebuildCommand::class)
        registerCommand(ChamberDestroyCommand::class)

        // Commands related to furnishings
        registerCommand(FurnishingCommand::class)

        // Commands related to available commands
        registerCommand(CommandCommand::class)

        // Commands related to config
        registerCommand(ConfigCommand::class)
        registerCommand(ConfigGetCommand::class)
        registerCommand(ConfigSetCommand::class)

        burrow.courier.subscribe(ChamberPostBuildEvent::class) {
            originalConfigMap[it.chamber.name] = chamber.config.clone()
        }

        burrow.courier.unsubscribe(ChamberPostDestroyEvent::class) {
            originalConfigMap.remove(it.chamber.name)
        }

        courier.subscribe(CommandNotFoundEvent::class) {
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

    /**
     * Retrieves all furnishing classes installed.
     */
    fun getFurnishingClasses(): List<FurnishingClass> =
        renovator.furnishings.values
            .map { it::class }
            .sortedBy { extractId(it) }
            .toList()

    /**
     * Retrieves all furnishing classes available for this chamber.
     */
    fun getAvailableFurnishingClasses(): List<FurnishingClass> {
        val isRoot = chamber.name == ChamberShepherd.ROOT_CHAMBER_NAME
        return burrow.warehouse.furnishingClasses
            .filter { isRoot || extractType(it) != Furniture.Type.ROOT }
            .sortedBy { extractId(it) }
            .toList()
    }

    private fun getTopLevelFurnishings(): List<Furnishing> =
        mutableSetOf<Furnishing?>()
            .apply {
                renovator.depTree.root.children.forEach { add(it.element) }
            }
            .filterNotNull()
            .toList()

    fun getFurnishingCommandClasses(
        onlyTopLevel: Boolean = false
    ): Map<Furnishing, List<CommandClass>> {
        val furnishings = when (onlyTopLevel) {
            true -> getTopLevelFurnishings()
            false -> renovator.furnishings.values
        }
        val map = mutableMapOf<Furnishing, List<CommandClass>>().apply {
            renovator.furnishings.values.forEach {
                if (!furnishings.contains(it)) {
                    return@forEach
                }

                this[it] = it.commandClasses
                    .sortedBy { commandClass -> commandClass.java.name }
                    .toList()
            }
        }

        return map
    }

    object ConfigKey {
        const val DESCRIPTION = "description"
    }

    object EventHandler {
        fun commandNotFoundEventHandler(event: CommandNotFoundEvent) {
            val commandName = event.commandName
            val outputStream = event.commandData.environment.outputStream
            StateWriterController(outputStream).let {
                it.getPrintWriter(OutputState.STDERR)
                    .println("Command not found: $commandName")
                it.getPrintWriter(OutputState.EXIT_CODE)
                    .println(ExitCode.USAGE)
            }
        }
    }
}