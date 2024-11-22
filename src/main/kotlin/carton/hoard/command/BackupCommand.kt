package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Option
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@CommandLine.Command(
    name = "backup",
    description = ["Create a backup for the hoard."]
)
class BackupCommand(data: CommandData) : Command(data) {
    @Option(names = ["-s", "--silent"])
    private var shouldBeSilent = false

    override fun call(): Int {
        val fileName = backup()
        if (!shouldBeSilent) {
            stdout.println("Backup file created: $fileName")
        }

        return ExitCode.OK
    }

    private fun backup(): String {
        val fileName = "hoard.${getCurrentDateString()}.json"
        use(Hoard::class).saveToHoardFile(chamber.rootPath.resolve(fileName))

        return fileName
    }

    private fun getCurrentDateString(): String {
        val formatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        return Instant.now()
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    companion object {
        @Suppress("SpellCheckingInspection")
        const val DATE_PATTERN = "yyyyMMddHHmmss"
        const val READABLE_DATE_PATTERN = "hh:mm:ss, MMM dd, yyyy"
    }
}