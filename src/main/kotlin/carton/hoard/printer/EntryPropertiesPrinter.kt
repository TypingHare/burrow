package burrow.carton.hoard.printer

import burrow.kernel.stream.Printer
import java.io.PrintWriter

data class EntryPropertiesPrinterContext(
    val properties: Map<String, String>
) {
    var paddingStart = 0
    var indentation = 4
}

class EntryPropertiesPrinter(
    writer: PrintWriter,
    context: EntryPropertiesPrinterContext
) : Printer<EntryPropertiesPrinterContext>(writer, context) {
    override fun print() {
        val properties = context.properties
        val paddingStartSpaces = " ".repeat(context.paddingStart)
        val indentationSpaces =
            " ".repeat(context.paddingStart + context.indentation)

        writer.println("$paddingStartSpaces{")
        for ((key, value) in properties) {
            writer.println("$indentationSpaces$key: $value")
        }
        writer.println("$paddingStartSpaces}")
    }
}