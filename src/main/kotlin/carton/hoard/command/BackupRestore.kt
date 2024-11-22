package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.kernel.chamber.BuildChamberException
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.*
import java.nio.file.Files
import kotlin.io.path.exists

@CommandLine.Command(
    name = "backup.restore",
    description = ["Restores a backup hoard."],
)
class BackupRestore(data: CommandData) : Command(data) {
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
        val filePath = chamber.rootPath.resolve(fileName)
        if (!filePath.exists()) {
            stderr.println("Backup file does not exist: $filePath")
            return ExitCode.USAGE
        }

        val hoardFilePath = hoard.hoardFilePath
        Files.delete(hoardFilePath)
        Files.copy(filePath, hoardFilePath)
        stdout.println("Restored hoard with: $filePath")

        if (delete) {
            Files.delete(filePath)
            stdout.println("Deleted the backup file: $filePath")
        }

        stdout.println("Rebuilding chamber...")
        chamber.rebuild()
        stdout.println("Rebuilt chamber successfully!")

        return ExitCode.OK
    }
}