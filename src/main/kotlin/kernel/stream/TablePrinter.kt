package burrow.kernel.stream

import org.jline.utils.AttributedString
import java.io.PrintWriter
import kotlin.math.max

class TablePrinterContext(
    val table: List<List<String>>,
    val maxColumns: Int,
) {
    val spacings = mutableListOf<Int>()
    var defaultSpacing = 2
}

class TablePrinter(
    writer: PrintWriter,
    context: TablePrinterContext
) : Printer<TablePrinterContext>(writer, context) {
    override fun print() {
        val table = context.table
        if (table.isEmpty()) {
            return
        }

        val spacings = context.spacings
        val defaultSpacing = context.defaultSpacing
        val cols = table[0].size
        val rows = table.size
        val widths = MutableList(cols) { 0 }
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val length = getLength(table[row][col])
                widths[col] = max(widths[col], length)
            }
        }

        for (row in 0 until rows) {
            var line = ""
            for (col in 0 until cols) {
                val spacing = spacings.getOrElse(col) { defaultSpacing }
                line += table[row][col].padEnd(widths[col] + spacing)
            }
            writer.println(line)
        }
    }

    private fun getLength(text: String): Int {
        return AttributedString.fromAnsi(text).length
    }
}