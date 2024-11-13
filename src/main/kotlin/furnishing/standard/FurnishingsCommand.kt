package burrow.furnishing.standard

import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.FurnishingDependencyTree
import picocli.CommandLine
import java.util.concurrent.atomic.AtomicInteger

@CommandLine.Command(
    name = "furnishings",
    description = ["Display the furnishing dependency tree."]
)
class FurnishingsCommand(data: CommandData) : Command(data) {
    @CommandLine.Option(
        names = ["-a", "--all"],
        description = ["Display a list of all available furnishings."],
        defaultValue = "false"
    )
    var all = false

    override fun call(): Int {
        if (all) displayFurnishingList() else displayFurnishingDependencyTree()

        return CommandLine.ExitCode.OK
    }

    private fun displayFurnishingDependencyTree() {
        val index = AtomicInteger(0)
        renovator.dependencyTree.root.children.onEach {
            printTree(it, index.getAndIncrement(), 0, 2)
        }
    }

    private fun printTree(
        node: FurnishingDependencyTree.Node,
        index: Int,
        indentation: Int,
        indentationIncrement: Int
    ) {
        val furnishing = node.furnishing ?: return
        val id = furnishing.getId()
        val label = furnishing.getLabel()
        stdout.println(" ".repeat(indentation) + "[$index] $label ($id)")

        val childIndex = AtomicInteger(0)
        node.children.onEach {
            printTree(
                it,
                childIndex.getAndIncrement(),
                indentation + indentationIncrement,
                indentationIncrement
            )
        }
    }

    private fun displayFurnishingList() {
        val furnishingClasses = burrow.furnishingWarehouse.furnishingClasses
        val index = AtomicInteger(0)
        for (furnishingClass in furnishingClasses) {
            val id = Furnishing.extractId(furnishingClass)
            val label = Furnishing.extractLabel(furnishingClass)
            stdout.println("[${index.getAndIncrement()}] $label ($id)")
        }
    }
}