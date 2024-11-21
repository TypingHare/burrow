package burrow.carton.hoard.printer

import burrow.carton.hoard.Entry
import burrow.carton.hoard.Hoard
import burrow.kernel.chamber.Chamber
import burrow.kernel.palette.Palette
import burrow.kernel.stream.Printer
import java.io.PrintWriter

abstract class EntryPrinter(writer: PrintWriter, context: EntryContext) :
    Printer<EntryContext>(writer, context) {
    protected fun getColoredOpenBrace(palette: Palette): String {
        return palette.color("{", Hoard.Highlights.BRACE)
    }

    protected fun getColoredCloseBrace(palette: Palette): String {
        return palette.color("}", Hoard.Highlights.BRACE)
    }
}

data class EntryContext(val entry: Entry, val chamber: Chamber) {
    var paddingStart = 0
    var indentation = 4
}

class EntryStorePrinter(writer: PrintWriter, context: EntryContext) :
    EntryPrinter(writer, context) {
    override fun print() {
        val entry = context.entry
        val properties =
            context.chamber.use(Hoard::class).convertStoreToProperties(entry)
        val palette = context.chamber.palette
        val entryId = context.entry.id

        val paddingStartSpaces = " ".repeat(context.paddingStart)
        val indentationSpaces =
            " ".repeat(context.paddingStart + context.indentation)

        val coloredId = palette.color(entryId.toString(), Hoard.Highlights.ID)
        val coloredOpenBrace = getColoredOpenBrace(palette)
        val coloredCloseBrace = getColoredCloseBrace(palette)
        writer.println("$paddingStartSpaces[$coloredId] $coloredOpenBrace")
        for ((key, value) in properties) {
            val coloredKey = palette.color(key, Hoard.Highlights.KEY)
            val coloredValue =
                palette.color(value, Hoard.Highlights.VALUE)
            writer.println("$indentationSpaces$coloredKey: $coloredValue")
        }
        writer.println(paddingStartSpaces + coloredCloseBrace)
    }
}

class EntryPropertiesPrinter(writer: PrintWriter, context: EntryContext) :
    EntryPrinter(writer, context) {
    override fun print() {
        val properties = context.entry.props
        val palette = context.chamber.palette

        val paddingStartSpaces = " ".repeat(context.paddingStart)
        val indentationSpaces =
            " ".repeat(context.paddingStart + context.indentation)

        val coloredOpenBrace = getColoredOpenBrace(palette)
        val coloredCloseBrace = getColoredCloseBrace(palette)
        writer.println(paddingStartSpaces + coloredOpenBrace)
        for ((key, value) in properties) {
            val coloredKey = palette.color(key, Hoard.Highlights.KEY)
            val coloredValue =
                palette.color("\"$value\"", Hoard.Highlights.VALUE)
            writer.println("$indentationSpaces$coloredKey: $coloredValue")
        }
        writer.println(paddingStartSpaces + coloredCloseBrace)
    }
}