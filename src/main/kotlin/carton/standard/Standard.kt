package burrow.carton.standard

import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.config.Config
import burrow.kernel.furnishing.DependencyTree
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.FurnishingClass
import burrow.kernel.furnishing.Furniture
import burrow.kernel.palette.Highlight

@Furniture(
    version = "0.0.0",
    description = "Standard Furnishing of all chambers.",
    type = Furniture.Type.COMPONENT
)
class Standard(chamber: Chamber) : Furnishing(chamber) {
    override fun assemble() {
        // Basic commands
        registerCommand(RootCommand::class)
        registerCommand(HelpCommand::class)

        // Furnishing commands
        registerCommand(FurnishingsCommand::class)

        // Command commands
        registerCommand(CommandsCommand::class)
    }

    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.ALIAS)
        config.addKey(ConfigKey.DESCRIPTION)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.ALIAS, chamber.name)
        config.setIfAbsent(ConfigKey.DESCRIPTION, "")
    }

    fun getFurnishingClassDependencyTree(): FurnishingClassDependencyTree {
        val dependencyTree = FurnishingClassDependencyTree()
        fun handle(
            furnishingNode: DependencyTree.Node<Furnishing>,
            furnishingClassNode: DependencyTree.Node<FurnishingClass>
        ) {
            furnishingNode.children.forEach {
                val nextFurnishingNode =
                    DependencyTree.Node(it.element!!::class)
                handle(it, nextFurnishingNode)
                furnishingClassNode.children.add(nextFurnishingNode)
            }
        }

        handle(renovator.dependencyTree.root, dependencyTree.root)
        return dependencyTree
    }

    fun getCompleteFurnishingClassDependencyTree(): FurnishingClassDependencyTree {
        val isRootChamber = chamber.name == Burrow.Standard.ROOT_CHAMBER_NAME
        val furnishingClasses = burrow.furnishingWarehouse.furnishingClasses
        val dependencyTree = FurnishingClassDependencyTree()
        furnishingClasses.filter {
            isRootChamber || extractType(it) != Furniture.Type.ROOT
        }.forEach { dependencyTree.root.add(it) }
        fun handle(node: DependencyTree.Node<FurnishingClass>) {
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
        renovator.dependencyTree.resolve {
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

        return furnishingClasses.filter {
            extractType(it) != Furniture.Type.ROOT
        }.toSet()
    }

    object ConfigKey {
        const val ALIAS = "alias"
        const val DESCRIPTION = "description"
    }

    object Highlights {
        val DEFAULT_FURNISHING = Highlight(67, 0, Highlight.Style.ITALIC)
        val INSTALLED_FURNISHING = Burrow.Highlights.FURNISHING
    }
}

class FurnishingClassDependencyTree : DependencyTree<FurnishingClass>()
