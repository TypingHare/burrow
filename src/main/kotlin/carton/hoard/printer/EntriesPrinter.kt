package burrow.carton.hoard.printer

import burrow.carton.hoard.Entry
import burrow.kernel.chamber.Chamber
import burrow.kernel.stream.Printer
import java.io.PrintWriter

class EntriesPrinter(writer: PrintWriter, context: EntriesContext) :
    Printer<EntriesContext>(writer, context) {
    override fun print() {
        val chamber = context.chamber
        val paddingStart = context.paddingStart
        val indentation = context.indentation
        val entryIndentation = context.entryIndentation

        writer.println("".repeat(paddingStart) + "[")
        for (entry in context.entries) {
            EntryStorePrinter(writer, EntryContext(entry, chamber).apply {
                this.paddingStart = paddingStart + indentation
                this.indentation = entryIndentation
            }).print()
        }
        writer.println("".repeat(paddingStart) + "]")
    }
}

data class EntriesContext(
    val entries: List<Entry>,
    val chamber: Chamber
) {
    var paddingStart = 0
    var indentation = 4
    var entryIndentation = 4
}