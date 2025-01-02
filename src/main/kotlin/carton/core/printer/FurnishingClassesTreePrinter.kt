package burrow.carton.core.printer

import burrow.kernel.furniture.DepTree
import burrow.kernel.furniture.FurnishingClass
import burrow.kernel.furniture.extractId
import burrow.kernel.stream.Printer
import java.io.PrintWriter

class FurnishingClassesTreePrinter(
    writer: PrintWriter,
    context: FurnishingClassesTreePrinterContext
) : Printer<FurnishingClassesTreePrinterContext>(writer, context) {
    override fun print() {
        val furnishingClassesTree = context.furnishingClassesTree
        val maxColumns = context.maxColumns

        fun printNode(
            node: DepTree.Node<FurnishingClass>,
            indentationWidth: Int
        ) {
            val indentation = when (indentationWidth) {
                0 -> ""
                else -> " ".repeat(indentationWidth) + "- "
            }
            node.children.forEach {
                if (it.element != null) {
                    val id = extractId(it.element)
                    writer.println("${indentation}${id}")
                }
                printNode(it, indentationWidth + 4)
            }
        }

        printNode(furnishingClassesTree.root, 0)
    }
}

data class FurnishingClassesTreePrinterContext(
    val furnishingClassesTree: DepTree<FurnishingClass>,
    val maxColumns: Int
)