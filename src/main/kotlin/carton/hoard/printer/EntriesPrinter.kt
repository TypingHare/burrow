package burrow.carton.hoard.printer

import burrow.kernel.stream.Printer
import java.io.PrintWriter

class EntriesPrinter(
    writer: PrintWriter,
    context: EntriesPrinterContext
) : Printer<EntriesPrinterContext>(writer, context) {
    override fun print() {
        val paddingStart = context.paddingStart
        val indentation = context.indentation
        val entryIndentation = context.entryIndentation

        writer.println("".repeat(paddingStart) + "[")
        for ((entryId, formattedProperties) in context.entries) {
            EntryPrinter(
                writer,
                EntryPrinterContext(entryId, formattedProperties).apply {
                    this.paddingStart = paddingStart + indentation
                    this.indentation = entryIndentation
                }).print()
        }
        writer.println("".repeat(paddingStart) + "]")
    }
}

data class EntriesPrinterContext(
    val entries: List<Pair<Int, Map<String, String>>>,
) {
    var paddingStart = 0
    var indentation = 4
    var entryIndentation = 4
}