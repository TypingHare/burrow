package burrow.carton.hoard.printer

import burrow.carton.hoard.BackupFile
import burrow.carton.hoard.command.backup.BackupCommand.Companion.DATE_PATTERN
import burrow.carton.hoard.command.backup.BackupCommand.Companion.READABLE_DATE_PATTERN
import burrow.kernel.stream.Printer
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BackupListPrinter(
    writer: PrintWriter,
    context: BackupListPrinterContext
) : Printer<BackupListPrinterContext>(writer, context) {
    override fun print() {
        val table = mutableListOf<List<String>>()
        context.backupFileList.forEachIndexed { index, backupFile ->
            val readableDateString =
                convertDateStringIntoReadableDateString(backupFile.dateString)
            table.add(
                listOf(
                    index.toString(),
                    backupFile.fileName,
                    "($readableDateString)"
                )
            )
        }

        TablePrinter(
            writer,
            TablePrinterContext(table, context.maxColumns)
        ).print()
    }

    private fun convertDateStringIntoReadableDateString(
        dateString: String
    ): String {
        val formatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        val readableFormatter =
            DateTimeFormatter.ofPattern(READABLE_DATE_PATTERN)

        return try {
            LocalDateTime.parse(dateString, formatter).format(readableFormatter)
        } catch (ex: Exception) {
            throw RuntimeException("Invalid date format")
        }
    }
}

data class BackupListPrinterContext(
    val backupFileList: List<BackupFile>,
    val maxColumns: Int,
)