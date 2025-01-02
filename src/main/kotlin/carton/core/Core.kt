package burrow.carton.core

import burrow.carton.core.command.*
import burrow.carton.core.command.config.ConfigCommand
import burrow.carton.core.command.config.ConfigGetCommand
import burrow.carton.core.command.config.ConfigSetCommand
import burrow.carton.core.command.furnishing.FurnishingAddCommand
import burrow.carton.core.command.furnishing.FurnishingListCommand
import burrow.carton.core.command.furnishing.FurnishingRemoveCommand
import burrow.carton.core.command.furnishing.FurnishingTreeCommand
import burrow.kernel.Burrow
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
        registerCommand(FurnishingListCommand::class)
        registerCommand(FurnishingTreeCommand::class)
        registerCommand(FurnishingAddCommand::class)
        registerCommand(FurnishingRemoveCommand::class)

        // Commands related to available commands
        registerCommand(CommandCommand::class)

        // Commands related to config
        registerCommand(ConfigCommand::class)
        registerCommand(ConfigGetCommand::class)
        registerCommand(ConfigSetCommand::class)

        courier.subscribe(CommandNotFoundEvent::class) {
            EventHandler.commandNotFoundEventHandler(it)
        }
    }

    @Throws(Exception::class)
    fun rebuildChamber(stderr: PrintWriter): Boolean {
        val chamberName = chamber.name
        try {
            chamberShepherd.rebuildChamber(chamberName)
            return true
        } catch (ex: Exception) {
            stderr.println(ex.message)
            stderr.println("Error during restarting. Now rolling back to the original blueprint.")

            val blueprint = chamberShepherd.getBluePrint(chamberName)
            val originalConfig = blueprint.config
            val originalFurnishingIds = blueprint.furnishingIds
            config.entries.putAll(originalConfig.entries)
            config.isModified.set(originalConfig.isModified.get())
            renovator.furnishingIds.clear()
            renovator.furnishingIds.addAll(originalFurnishingIds)

            stderr.println("Blueprint has been restored.")

            return false
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

    fun getFurnishingClassesTree(): DepTree<FurnishingClass> {
        fun handle(
            furnishingNode: DepTree.Node<Furnishing>,
            furnishingClassNode: DepTree.Node<FurnishingClass>
        ) {
            furnishingNode.children.forEach {
                val nextFurnishingNode =
                    DepTree.Node(it.element!!::class)
                handle(it, nextFurnishingNode)
                furnishingClassNode.children.add(nextFurnishingNode)
            }
        }

        return DepTree<FurnishingClass>().apply {
            handle(renovator.depTree.root, root)
        }
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