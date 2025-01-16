package burrow.carton.core.printer

import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.extractId
import burrow.kernel.stream.Printer
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import burrow.kernel.terminal.CommandClass
import burrow.kernel.terminal.extractCommandName
import burrow.kernel.terminal.extractHeader
import java.io.PrintWriter
import kotlin.math.max

class FurnishingCommandClassesPrinterContext(
    val furnishingCommandClasses: Map<Furnishing, List<CommandClass>>,
    val maxColumns: Int
)

class FurnishingCommandClassesPrinter(
    writer: PrintWriter,
    context: FurnishingCommandClassesPrinterContext
) : Printer<FurnishingCommandClassesPrinterContext>(writer, context) {
    override fun print() {
        var longestCommandNameLength = 0
        for ((_, commandClasses) in context.furnishingCommandClasses) {
            for (commandClass in commandClasses) {
                val commandName = extractCommandName(commandClass)
                longestCommandNameLength =
                    max(longestCommandNameLength, commandName.length)
            }
        }

        var hasPrevious = false
        for ((furnishing, commandClasses) in context.furnishingCommandClasses) {
            if (commandClasses.isEmpty()) {
                continue
            }

            if (hasPrevious) {
                writer.println()
            } else {
                hasPrevious = true
            }

            var commandNameLength = 0
            for (commandClass in commandClasses) {
                val commandName = extractCommandName(commandClass)
                commandNameLength = max(commandNameLength, commandName.length)
            }

            writer.println("[" + extractId(furnishing::class) + "]")

            val table = mutableListOf<List<String>>().apply {
                commandClasses.forEach { commandClass ->
                    val commandName = extractCommandName(commandClass)
                    val commandHeader = extractHeader(commandClass)
                    add(listOf(commandName, commandHeader))
                }
            }

            val context = TablePrinterContext(table, context.maxColumns).apply {
                defaultSpacing += longestCommandNameLength - commandNameLength
            }
            TablePrinter(writer, context).print()
        }
    }
}