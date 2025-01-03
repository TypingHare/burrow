package burrow.carton.core.printer

import burrow.carton.core.help.CommandOption
import burrow.carton.core.help.CommandParameter
import burrow.kernel.stream.Printer
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import java.io.PrintWriter

class SynopsisPrinter(
    writer: PrintWriter,
    context: SynopsisPrintContext
) : Printer<SynopsisPrintContext>(writer, context) {
    override fun print() {
        val commandString = context.commandString
        val parameters = context.commandParameters
        val options = context.commandOptions
        val leftLength = context.commandString.length
        val spacing = 1
        val maxRightLength = context.maxColumns - leftLength - spacing

        val table = mutableListOf<List<String>>()
        var line = ""
        var lineLength = 0
        fun append(string: String) {
            if (lineLength + string.length >= maxRightLength) {
                table.add(listOf("", line))
                line = ""
                lineLength = 0
            }

            line += " $string"
            lineLength += string.length + 1
        }

        for (option in options) {
            val string = when (option.defaultValue != "") {
                true -> "[${option.label}=${option.defaultValue}]"
                false -> "[${option.label}]"
            }
            append(string)
        }

        for (parameter in parameters) {
            when (parameter.isOptional) {
                true -> "[<${parameter.label}>]"
                false -> "<${parameter.label}>"
            }.let { append(it) }
        }

        table.add(listOf("", line))

        table[0] =
            mutableListOf(commandString).apply { addAll(table[0].drop(1)) }

        TablePrinter(
            writer,
            TablePrinterContext(
                table,
                context.maxColumns
            ).apply { this.defaultSpacing = spacing }
        ).print()
    }
}

data class SynopsisPrintContext(
    val commandString: String,
    val commandParameters: List<CommandParameter>,
    val commandOptions: List<CommandOption>,
    val maxColumns: Int,
)