package burrow.carton.hoard.command.backup

import burrow.carton.hoard.Hoard
import burrow.kernel.chamber.BuildChamberException
import burrow.kernel.terminal.*
import java.nio.file.Files
import kotlin.io.path.exists

@BurrowCommand(
    name = "backup.restore",
    description = ["Restores a backup hoard."],
)
class BackupRestoreCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "<file-name>",
        description = ["The file name of the backup used to restore."]
    )
    private var fileName = ""

    @Option(
        names = ["--delete", "-d"],
        description = ["Deletes the backup file after restoring."]
    )
    private var delete = false

    @Throws(BuildChamberException::class)
    override fun call(): Int {
        val hoard = use(Hoard::class)
        val filePath = chamber.getPath().resolve(fileName)
        if (!filePath.exists()) {
            stderr.println("Backup file does not exist: $filePath")
            return ExitCode.USAGE
        }

        val hoardFilePath = hoard.getPath()
        Files.delete(hoardFilePath)
        Files.copy(filePath, hoardFilePath)
        stdout.println("Restored hoard with: $filePath")

        if (delete) {
            Files.delete(filePath)
            stdout.println("Deleted the backup file: $filePath")
        }

        stdout.println("Rebuilding chamber...")
        chamberShepherd.rebuildChamber(chamber.name)
        stdout.println("Rebuilt chamber successfully!")

        return ExitCode.OK
    }
}