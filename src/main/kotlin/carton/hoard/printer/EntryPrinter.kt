package burrow.carton.hoard.printer

import burrow.kernel.stream.Printer
import java.io.PrintWriter

data class EntryPrinterContext(
    val entryId: Int,
    val formattedProperties: Map<String, String>
) {
    var paddingStart = 0
    var indentation = 4
}

class EntryPrinter(
    writer: PrintWriter,
    context: EntryPrinterContext
) : Printer<EntryPrinterContext>(writer, context) {
    override fun print() {
        val id = context.entryId
        val properties = context.formattedProperties
        val paddingStartSpaces = " ".repeat(context.paddingStart)
        val indentationSpaces =
            " ".repeat(context.paddingStart + context.indentation)

        writer.println("$paddingStartSpaces($id) {")
        for ((key, value) in properties) {
            writer.println("$indentationSpaces$key: $value")
        }
        writer.println("$paddingStartSpaces}")
    }
}