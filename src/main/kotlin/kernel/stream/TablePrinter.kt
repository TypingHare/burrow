package burrow.kernel.stream

import java.io.PrintWriter
import org.jline.utils.AttributedString
import kotlin.math.max

class TablePrinterContext(
    val table: List<List<String>>,
    val maxColumns: Int,
) {
    var spacing = 2
}

class TablePrinter(
    writer: PrintWriter,
    context: TablePrinterContext
) : Printer<TablePrinterContext>(writer, context) {
    override fun print() {
        val table = context.table
        val spacing = context.spacing
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
            for (col in 0 until cols) {
                writer.print(table[row][col].padEnd(widths[col] + spacing))
            }
            writer.println()
        }
    }

    private fun getLength(text: String): Int {
        return AttributedString.fromAnsi(text).length
    }
}