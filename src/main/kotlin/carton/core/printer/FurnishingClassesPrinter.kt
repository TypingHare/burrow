package burrow.carton.core.printer

import burrow.kernel.furniture.FurnishingClass
import burrow.kernel.furniture.extractDescription
import burrow.kernel.furniture.extractId
import burrow.kernel.stream.Printer
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import java.io.PrintWriter

class FurnishingClassesPrinterContext(
    val furnishingClasses: List<FurnishingClass>,
    val maxColumns: Int
)

class FurnishingClassesPrinter(
    writer: PrintWriter,
    context: FurnishingClassesPrinterContext
) : Printer<FurnishingClassesPrinterContext>(writer, context) {
    override fun print() {
        val table = mutableListOf<List<String>>()
        context.furnishingClasses.forEach { furnishingClass ->
            val id = extractId(furnishingClass)
            val description = extractDescription(furnishingClass)
            table.add(listOf(id, description))
        }

        TablePrinter(
            writer,
            TablePrinterContext(table, context.maxColumns)
        ).print()
    }
}