package burrow.carton.hoard.command.backup

import burrow.carton.hoard.Hoard
import burrow.kernel.terminal.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@BurrowCommand(
    name = "backup",
    header = ["Create a backup for the hoard."]
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
        use(Hoard::class).storage.saveTo(chamber.getPath().resolve(fileName))

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
        const val READABLE_DATE_PATTERN = "MMM dd hh:mm"
    }
}