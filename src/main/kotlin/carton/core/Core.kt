package burrow.carton.core

import burrow.carton.core.command.*
import burrow.carton.core.command.chamber.ChamberDestroyCommand
import burrow.carton.core.command.chamber.ChamberRebuildCommand
import burrow.carton.core.command.config.ConfigCommand
import burrow.carton.core.command.config.ConfigGetCommand
import burrow.carton.core.command.config.ConfigSetCommand
import burrow.carton.core.command.furnishing.*
import burrow.kernel.Burrow
import burrow.kernel.config.Config
import burrow.kernel.furniture.*
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.terminal.CommandClass
import burrow.kernel.terminal.CommandNotFoundEvent
import burrow.kernel.terminal.Interpreter
import java.io.PrintWriter

@Furniture(
    version = Burrow.VERSION,
    description = "The core furnishing.",
    type = Furniture.Type.COMPONENT
)
class Core(renovator: Renovator) : Furnishing(renovator) {
    override fun prepareConfig(config: Config) {
        registerConfigKey(ConfigKey.DESCRIPTION)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.DESCRIPTION, Default.DESCRIPTION)
    }

    override fun assemble() {
        // Basic commands
        registerCommand(DefaultCommand::class)
        registerCommand(RootCommand::class)
        registerCommand(HelpCommand::class)

        // Chamber commands
        registerCommand(ChamberCommand::class)
        registerCommand(ChamberRebuildCommand::class)
        registerCommand(ChamberDestroyCommand::class)

        // Furnishing commands
        registerCommand(FurnishingListCommand::class)
        registerCommand(FurnishingTreeCommand::class)
        registerCommand(FurnishingAddCommand::class)
        registerCommand(FurnishingRemoveCommand::class)
        registerCommand(FurnishingMatchCommand::class)

        // Commands related to chamber commands
        registerCommand(CommandCommand::class)

        // Config commands
        registerCommand(ConfigCommand::class)
        registerCommand(ConfigGetCommand::class)
        registerCommand(ConfigSetCommand::class)

        courier.unsubscribe(
            CommandNotFoundEvent::class,
            Interpreter.EventHandler::commandNotFoundEventHandler
        )
        courier.subscribe(
            CommandNotFoundEvent::class,
            EventHandler::commandNotFoundEventHandler
        )
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

    private fun getTopLevelFurnishings(): List<Furnishing> =
        mutableSetOf<Furnishing?>()
            .apply {
                renovator.depTree.root.children.forEach { add(it.element) }
            }
            .filterNotNull()
            .toList()

    object Default {
        const val DESCRIPTION = "<No description>"
    }

    object ConfigKey {
        const val DESCRIPTION = "description"
    }

    companion object {
        const val DEFAULT_COMMAND_NAME = "\$default\$"
    }

    object EventHandler {
        fun commandNotFoundEventHandler(event: CommandNotFoundEvent) {
            event.commandData.let {
                it.chamber.interpreter.execute(DEFAULT_COMMAND_NAME, it)
            }
        }
    }
}