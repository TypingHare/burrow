package burrow.carton.standard.printtask

import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.FurnishingClass
import burrow.kernel.palette.Highlight
import burrow.kernel.palette.Palette
import burrow.kernel.stream.PrintTask
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger

class FurnishingClassesPrintContext(
    val furnishingClasses: List<FurnishingClass>,
    val palette: Palette,
    val labelHighlight: Highlight,
) {
    var shouldPrintFurnishingId = false
}

class FurnishingClassesPrintTask(
    writer: PrintWriter,
    context: FurnishingClassesPrintContext
) : PrintTask<FurnishingClassesPrintContext>(writer, context) {
    override fun print() {
        val index = AtomicInteger(0)
        val palette = context.palette

        context.furnishingClasses.forEach { furnishingClass ->
            val id = Furnishing.extractId(furnishingClass)
            val label = Furnishing.extractLabel(furnishingClass)
            val description = Furnishing.extractDescription(furnishingClass)
            val coloredLabel = palette.color(label, context.labelHighlight)
            if (context.shouldPrintFurnishingId) {
                writer.println("[$index] $coloredLabel ($id)  $description")
            } else {
                writer.println("[$index] $coloredLabel  $description")
            }

            index.getAndIncrement()
        }
    }
}