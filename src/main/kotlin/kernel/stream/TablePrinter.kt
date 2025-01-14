package burrow.kernel.stream

import org.jline.utils.AttributedString
import java.io.PrintWriter
import kotlin.math.max

class TablePrinterContext(
    val table: List<List<String>>,
    val maxColumns: Int,
) {
    val spacings = mutableListOf<Int>()
    var defaultSpacing = 4
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

        val widthTable = mutableListOf<List<Int>>()
        for (row in 0 until rows) {
            val line = mutableListOf<Int>().apply { widthTable.add(this) }
            for (col in 0 until cols) {
                line.add(getWidth(table[row][col]))
            }
        }

        val maxWidths = MutableList(cols) { 0 }
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                maxWidths[col] = max(maxWidths[col], widthTable[row][col])
            }
        }

        for (row in 0 until rows) {
            var line = ""
            for (col in 0 until cols) {
                val spacing = spacings.getOrElse(col) { defaultSpacing }
                line += table[row][col] + " ".repeat(maxWidths[col] - widthTable[row][col] + spacing)
            }
            writer.println(line)
        }
    }

    private fun getWidth(text: String): Int {
        val plainText = AttributedString.fromAnsi(text)
        var width = 0
        for (char in plainText) {
            width += if (char.isChineseCharacter()) {
                2
            } else {
                1
            }
        }

        return width
    }
}

fun Char.isChineseCharacter(): Boolean {
    val codePoint = this.code
    return (codePoint in 0x4E00..0x9FFF) ||
            (codePoint in 0x3400..0x4DBF) ||
            (codePoint in 0x20000..0x2A6DF)
}