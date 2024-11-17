package burrow.carton.hoard.printer

import burrow.carton.hoard.Entry
import burrow.carton.hoard.Hoard
import burrow.kernel.chamber.Chamber
import burrow.kernel.stream.Printer
import java.io.PrintWriter

abstract class EntryPrinter(writer: PrintWriter, context: EntryContext) :
    Printer<EntryContext>(writer, context) {
}

data class EntryContext(val entry: Entry, val chamber: Chamber)

class EntryStorePrinter(writer: PrintWriter, context: EntryContext) :
    EntryPrinter(writer, context) {
    override fun print() {
        val store = context.entry.store
        val palette = context.chamber.palette
        val entryId = context.entry.id

        val coloredId = palette.color(entryId.toString(), Hoard.Highlights.ID)
        val coloredOpenBrace = palette.color("{", Hoard.Highlights.BRACE)
        val coloredCloseBrace = palette.color("}", Hoard.Highlights.BRACE)
        writer.println("[$coloredId] $coloredOpenBrace")
        for ((key, value) in store) {
            val coloredKey = palette.color(key, Hoard.Highlights.KEY)
            val coloredValue =
                palette.color(value.toString(), Hoard.Highlights.VALUE)
            writer.println("${" ".repeat(4)}$coloredKey: $coloredValue")
        }
        writer.println(coloredCloseBrace)
    }
}

class EntryPropertiesPrinter(writer: PrintWriter, context: EntryContext) :
    EntryPrinter(writer, context) {
    override fun print() {
        val properties = context.entry.properties
        val palette = context.chamber.palette

        writer.println(palette.color("{", Hoard.Highlights.BRACE))
        for ((key, value) in properties) {
            val coloredKey = palette.color(key, Hoard.Highlights.KEY)
            val coloredValue =
                palette.color("\"$value\"", Hoard.Highlights.VALUE)
            writer.println("${" ".repeat(4)}$coloredKey: $coloredValue")
        }
        writer.println(palette.color("}", Hoard.Highlights.BRACE))
    }
}