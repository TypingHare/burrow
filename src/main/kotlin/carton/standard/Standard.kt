package burrow.carton.standard

import burrow.carton.standard.command.*
import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberPostBuildEvent
import burrow.kernel.chamber.ChamberPostDestroyEvent
import burrow.kernel.command.CommandClass
import burrow.kernel.config.Config
import burrow.kernel.furnishing.DepTree
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.FurnishingClass
import burrow.kernel.furnishing.annotation.Furniture
import burrow.kernel.palette.Highlight
import java.io.PrintWriter

@Furniture(
    version = Burrow.VERSION.NAME,
    description = "Standard Furnishing of all chambers.",
    type = Furniture.Type.COMPONENT
)
class Standard(chamber: Chamber) : Furnishing(chamber) {
    private val configMap = mutableMapOf<String, Config>()

    override fun assemble() {
        // Basic commands
        registerCommand(TestCommand::class)
        registerCommand(RootCommand::class)
        registerCommand(HelpCommand::class)

        // Chamber commands
        registerCommand(ChamberRebuildCommand::class)

        // Furnishing commands
        registerCommand(FurnishingCommand::class)
        registerCommand(FurnishingAddCommand::class)
        registerCommand(FurnishingRemoveCommand::class)

        // Command commands
        registerCommand(CommandCommand::class)

        // Config commands
        registerCommand(ConfigCommand::class)
        registerCommand(ConfigSetCommand::class)
        registerCommand(ConfigGetCommand::class)

        burrow.affairManager.subscribe(ChamberPostBuildEvent::class) {
            configMap[it.chamber.name] = chamber.config.clone()
        }

        burrow.affairManager.subscribe(ChamberPostDestroyEvent::class) {
            configMap.remove(it.chamber.name)
        }
    }

    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.ALIAS)
        config.addKey(ConfigKey.DESCRIPTION)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.ALIAS, chamber.name)
        config.setIfAbsent(ConfigKey.DESCRIPTION, "")
    }

    fun getTopLevelFurnishingsCommandClasses(): FurnishingsCommandClasses {
        val map = mutableMapOf<Furnishing, List<CommandClass>>()
        chamber.renovator.depTree.root.children.forEach {
            val furnishing = it.element ?: return@forEach
            map[furnishing] = furnishing.commands.toList()
        }

        return map
    }

    fun getAllFurnishingsCommandClasses(): FurnishingsCommandClasses {
        val map = mutableMapOf<Furnishing, List<CommandClass>>()
        chamber.renovator.furnishings.values.forEach {
            map[it] = it.commands.toList()
        }

        return map
    }

    fun getFurnishingClassDependencyTree(): FurnishingClassDependencyTree {
        val dependencyTree = FurnishingClassDependencyTree()
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

        handle(renovator.depTree.root, dependencyTree.root)
        return dependencyTree
    }

    fun getCompleteFurnishingClassDependencyTree(): FurnishingClassDependencyTree {
        val isRootChamber = chamber.name == Burrow.Standard.ROOT_CHAMBER_NAME
        val furnishingClasses = burrow.furnishingWarehouse.furnishingClasses
        val dependencyTree = FurnishingClassDependencyTree()
        furnishingClasses.filter {
            isRootChamber || extractType(it) != Furniture.Type.ROOT
        }.forEach { dependencyTree.root.add(it) }
        fun handle(node: DepTree.Node<FurnishingClass>) {
            node.children.forEach {
                val furnishingClass = it.element!!
                extractDependencies(furnishingClass).filter {
                    isRootChamber || extractType(furnishingClass) != Furniture.Type.ROOT
                }.forEach { dependency -> it.add(dependency) }
                handle(it)
            }
        }

        handle(dependencyTree.root)
        return dependencyTree
    }

    /**
     * Retrieves all dependency furnishing classes.
     */
    fun getFurnishingClasses(): Set<FurnishingClass> {
        val furnishingClassSet = mutableSetOf<FurnishingClass>()
        renovator.depTree.resolve {
            furnishingClassSet.add(it::class)
        }

        return furnishingClassSet
    }

    /**
     * Retrieves all available furnishing classes.
     */
    fun getAvailableFurnishingClasses(): Set<FurnishingClass> {
        val furnishingClasses = burrow.furnishingWarehouse.furnishingClasses
        if (chamber.name == Burrow.Standard.ROOT_CHAMBER_NAME) {
            return furnishingClasses.toSet()
        }

        return furnishingClasses
            .filter { extractType(it) != Furniture.Type.ROOT }
            .toSet()
    }

    fun rebuildChamberPreservingFurnishingList(
        originalFurnishingIds: Set<String>,
        stderr: PrintWriter
    ): Boolean {
        try {
            chamber.rebuild()
            return true
        } catch (ex: Exception) {
            ex.printStackTrace()
            stderr.println(ex.message)
            stderr.println("Error during restarting. Now Roll back to the original furnishing list.")
            renovator.saveFurnishingIds(originalFurnishingIds)
            burrow.chamberShepherd.buildChamber(chamber.name)
            return false
        }
    }

    @Throws(Exception::class)
    fun rebuildChamberPreservingConfig(stderr: PrintWriter) {
        try {
            chamber.rebuild()
        } catch (ex: Exception) {
            stderr.println(ex.message)
            stderr.println("Error during restarting. Now roll back to the original config.")

            val originalConfig = configMap[chamber.name]!!
            config.entries.putAll(originalConfig.entries)
            config.isModified.set(originalConfig.isModified.get())

            throw ex
        }
    }

    object ConfigKey {
        const val ALIAS = "alias"
        const val DESCRIPTION = "description"
    }

    object Highlights {
        val DEFAULT_FURNISHING = Highlight(67, 0, Highlight.Style.ITALIC)
        val INSTALLED_FURNISHING = Burrow.Highlights.FURNISHING
        val CONFIG_KEY = Highlight(81, 0, 0)
        val CONFIG_VALUE = Highlight(222, 0, 0)
    }
}

class FurnishingClassDependencyTree : DepTree<FurnishingClass>()
