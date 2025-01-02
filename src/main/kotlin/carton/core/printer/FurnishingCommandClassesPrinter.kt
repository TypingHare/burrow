package burrow.carton.core.printer

import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.extractId
import burrow.kernel.stream.Printer
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import burrow.kernel.terminal.CommandClass
import burrow.kernel.terminal.extractCommandDescription
import burrow.kernel.terminal.extractCommandName
import java.io.PrintWriter

class FurnishingCommandClassesPrinterContext(
    val furnishingCommandClasses: Map<Furnishing, List<CommandClass>>,
    val maxColumns: Int
)

class FurnishingCommandClassesPrinter(
    writer: PrintWriter,
    context: FurnishingCommandClassesPrinterContext
) : Printer<FurnishingCommandClassesPrinterContext>(writer, context) {
    override fun print() {
        for ((furnishing, commandClasses) in context.furnishingCommandClasses) {
            if (commandClasses.isEmpty()) {
                continue
            }

            writer.println("[" + extractId(furnishing::class) + "]")

            val table = mutableListOf<List<String>>().apply {
                commandClasses.forEach { commandClass ->
                    val commandName = extractCommandName(commandClass)
                    val commandDescription =
                        extractCommandDescription(commandClass)
                    add(listOf(commandName, commandDescription))
                }
            }
            TablePrinter(
                writer,
                TablePrinterContext(table, context.maxColumns)
            ).print()
        }
    }
}