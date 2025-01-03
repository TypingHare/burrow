package burrow.carton.hoard.command.backup

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.printer.BackupListPrinter
import burrow.carton.hoard.printer.BackupListPrinterContext
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "backup.list",
    header = ["Displays the list of backup hoards."]
)
class BackupListCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val backupFileList = use(Hoard::class).getBackupFileList()
        val sortedBackupFileList = backupFileList.stream().sorted().toList()
        BackupListPrinter(
            stdout,
            BackupListPrinterContext(
                sortedBackupFileList,
                getTerminalWidth()
            )
        ).print()

        return ExitCode.OK
    }
}