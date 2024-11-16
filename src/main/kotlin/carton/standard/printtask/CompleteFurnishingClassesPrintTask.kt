package burrow.carton.standard.printtask

import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.FurnishingClass
import burrow.kernel.palette.Highlight
import burrow.kernel.palette.Palette
import burrow.kernel.stream.PrintTask
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger


class CompleteFurnishingClassesPrintContext(
    val furnishingClasses: List<FurnishingClass>,
    val installedFurnishingClasses: Set<FurnishingClass>,
    val palette: Palette,
    val defaultLabelHighlight: Highlight,
    val installedLabelHighlight: Highlight,
) {
    var shouldPrintFurnishingId = false
}

class CompleteFurnishingClassesPrintTask(
    writer: PrintWriter,
    context: CompleteFurnishingClassesPrintContext
) : PrintTask<CompleteFurnishingClassesPrintContext>(writer, context) {
    override fun print() {
        val index = AtomicInteger(0)
        val palette = context.palette
        val installedFurnishingClasses = context.installedFurnishingClasses
        val defaultLabelHighlight = context.defaultLabelHighlight
        val installedLabelHighlight = context.installedLabelHighlight

        context.furnishingClasses.forEach { furnishingClass ->
            val id = Furnishing.extractId(furnishingClass)
            val label = Furnishing.extractLabel(furnishingClass)
            val description = Furnishing.extractDescription(furnishingClass)
            val labelHighlight =
                if (furnishingClass in installedFurnishingClasses)
                    installedLabelHighlight
                else
                    defaultLabelHighlight
            val coloredLabel = palette.color(label, labelHighlight)
            if (context.shouldPrintFurnishingId) {
                writer.println("[$index] $coloredLabel ($id)  $description")
            } else {
                writer.println("[$index] $coloredLabel  $description")
            }

            index.getAndIncrement()
        }
    }
}