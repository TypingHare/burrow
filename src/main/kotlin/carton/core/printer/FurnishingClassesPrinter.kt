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
) {
    var addStarBeforeInstalledFurnishing: Boolean = false
    var installedFurnishingClasses: List<FurnishingClass> = emptyList()
}

class FurnishingClassesPrinter(
    writer: PrintWriter,
    context: FurnishingClassesPrinterContext
) : Printer<FurnishingClassesPrinterContext>(writer, context) {
    override fun print() {
        val table = mutableListOf<List<String>>()
        val installedFurnishingClasses = context.installedFurnishingClasses
        context.furnishingClasses.forEach { furnishingClass ->
            val id = extractId(furnishingClass)
            val description = extractDescription(furnishingClass)
            when (context.addStarBeforeInstalledFurnishing) {
                true -> {
                    val isInstalled =
                        installedFurnishingClasses.contains(furnishingClass)
                    val starString = if (isInstalled) "*" else ""
                    listOf(starString, id, description)
                }
                false -> listOf(id, description)
            }.let { table.add(it) }
        }

        TablePrinter(
            writer,
            TablePrinterContext(table, context.maxColumns).apply {
                if (context.addStarBeforeInstalledFurnishing) {
                    spacings.add(1)
                    spacings.add(2)
                }
            }
        ).print()
    }
}