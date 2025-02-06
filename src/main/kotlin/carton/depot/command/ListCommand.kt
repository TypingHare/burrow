package burrow.carton.depot.command

import burrow.carton.haystack.Haystack
import burrow.carton.hoard.Hoard
import burrow.carton.launcher.Launcher
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode
import java.io.File

@BurrowCommand(
    name = "list",
    header = ["Displays all repositories along with their information."],
)
class ListCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val entries = use(Hoard::class).storage.getAllEntries()
        val table = mutableListOf(
            listOf("ID", "Name", "Absolute Path", "Exist", "Opener")
        )

        entries.forEach {
            val id = it.id.toString()
            val relativePath = it.get<String>(Haystack.EntryKey.NAME)!!
            val absolutePath = it.get<String>(Haystack.EntryKey.ABSOLUTE_PATH)!!
            val exist = if (File(absolutePath).exists()) "yes" else "no"
            val opener = it.get<String>(Launcher.EntryKey.LAUNCHER)!!
            table.add(listOf(id, relativePath, absolutePath, exist, opener))
        }

        val context = TablePrinterContext(table, getTerminalWidth())
        TablePrinter(stdout, context).print()

        return ExitCode.OK
    }
}