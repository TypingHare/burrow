package burrow.carton.standard.printer

import burrow.carton.standard.FurnishingClassDependencyTree
import burrow.kernel.furnishing.DependencyTree
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.FurnishingClass
import burrow.kernel.palette.Highlight
import burrow.kernel.palette.Palette
import burrow.kernel.stream.Printer
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger

class FurnishingClassDependencyTreePrintContext(
    val dependencyTree: FurnishingClassDependencyTree,
    val palette: Palette,
    val labelHighlight: Highlight,
) {
    var indentationStep = 4
    var shouldPrintFurnishingId = false
}

class FurnishingClassDependencyTreePrintTask(
    writer: PrintWriter,
    context: FurnishingClassDependencyTreePrintContext
) : Printer<FurnishingClassDependencyTreePrintContext>(writer, context) {
    override fun print() {
        val index = AtomicInteger(0)
        context.dependencyTree.root.children.onEach {
            printNode(
                it,
                index.getAndIncrement(),
                0,
                context.indentationStep
            )
        }
    }

    private fun printNode(
        node: DependencyTree.Node<FurnishingClass>,
        index: Int,
        indentation: Int,
        indentationIncrement: Int
    ) {
        val palette = context.palette
        val furnishingClass = node.element ?: return
        val id = Furnishing.extractId(furnishingClass)
        val label = Furnishing.extractLabel(furnishingClass)
        val description = Furnishing.extractDescription(furnishingClass)
        val coloredLabel = palette.color(label, context.labelHighlight)
        if (context.shouldPrintFurnishingId) {
            writer.println(
                " ".repeat(indentation) + "[$index] $coloredLabel ($id)  $description"
            )
        } else {
            writer.println(
                " ".repeat(indentation) + "[$index] $coloredLabel  $description"
            )
        }

        val childIndex = AtomicInteger(0)
        node.children.onEach {
            printNode(
                it,
                childIndex.getAndIncrement(),
                indentation + indentationIncrement,
                indentationIncrement
            )
        }
    }
}