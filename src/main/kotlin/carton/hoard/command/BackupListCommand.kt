package burrow.carton.hoard.command

import burrow.carton.hoard.command.BackupCommand.Companion.DATE_PATTERN
import burrow.carton.hoard.command.BackupCommand.Companion.READABLE_DATE_PATTERN
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger


@CommandLine.Command(
    name = "backup.list",
    description = ["Displays the list of backup hoards."]
)
class BackupListCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val filenames = chamber.rootPath.toFile()
            .listFiles { it -> it.isFile() }
            ?.map { it.name } ?: emptyList()
        val backupFileList = mutableListOf<BackupFile>()
        for (filename in filenames) {
            val pattern = Regex("""^hoard.(\d+).json${'$'}""")
            val matcher = pattern.find(filename) ?: continue
            backupFileList.add(BackupFile(filename, matcher.groupValues[1]))
        }

        val sortedBackupFileList = backupFileList.stream().sorted().toList()
        val index = AtomicInteger(0)
        for (backupFile in sortedBackupFileList) {
            val filename = backupFile.filename
            val readableDateString =
                convertDateStringIntoReadableDateString(backupFile.dateString)
            stdout.println("[${index.getAndDecrement()}] $filename ($readableDateString)")
        }

        return ExitCode.OK
    }

    private fun convertDateStringIntoReadableDateString(dateString: String): String {
        val formatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        val readableFormatter =
            DateTimeFormatter.ofPattern(READABLE_DATE_PATTERN)

        return try {
            LocalDateTime.parse(dateString, formatter).format(readableFormatter)
        } catch (ex: Exception) {
            throw RuntimeException("Invalid date format")
        }
    }

    data class BackupFile(val filename: String, val dateString: String)
}