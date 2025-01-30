package burrow.carton.hoard.command.backup

import burrow.carton.hoard.Hoard
import burrow.kernel.terminal.*
import java.nio.file.Files

@BurrowCommand(
    name = "backup.del",
    header = ["Deletes a backup hoard."]
)
class BackupDelCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The name of the backup file to delete."],
        defaultValue = ""
    )
    private var fileName = ""

    @Option(
        names = ["--all", "-a"],
        description = ["Deletes all backup files."]
    )
    private var shouldDeleteAll = false

    override fun call(): Int {
        val backupFileList = use(Hoard::class).getBackupFileList()
        if (shouldDeleteAll) {
            backupFileList.forEach {
                val filePath = chamber.getPath().resolve(it.fileName)
                if (Files.deleteIfExists(filePath)) {
                    stdout.println("Deleted backup hoard file: $filePath")
                }
            }

            return ExitCode.OK
        }

        if (fileName.isEmpty()) {
            stderr.println("Please specify a backup file name or use the \"--all\" option.")
            ExitCode.USAGE
        }

        if (!backupFileList.any { it.fileName == fileName }) {
            stderr.println("Backup hoard file does not exist: $fileName")
            ExitCode.USAGE
        }

        Files.deleteIfExists(chamber.getPath().resolve(fileName))

        return ExitCode.OK
    }
}