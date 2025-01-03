package burrow.carton.depot.command

import burrow.carton.haystack.Haystack
import burrow.carton.haystack.HaystackOpener
import burrow.carton.hoard.Hoard
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode
import java.io.File

@BurrowCommand(
    name = "list",
    header = ["Displays all entries in table format."],
)
class ListCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val entries = use(Hoard::class).getAllEntries()
        val table = mutableListOf(
            listOf("ID", "Relative Path", "Absolute Path", "Exist", "Opener")
        )

        entries.forEach {
            val id = it.id.toString()
            val relativePath = it.get<String>(Haystack.EntryKey.RELATIVE_PATH)!!
            val absolutePath = it.get<String>(Haystack.EntryKey.ABSOLUTE_PATH)!!
            val exist = if (File(absolutePath).exists()) "yes" else "no"
            val opener = it.get<String>(HaystackOpener.EntryKey.OPENER)!!
            table.add(listOf(id, relativePath, absolutePath, exist, opener))
        }

        TablePrinter(
            stdout,
            TablePrinterContext(table, getTerminalSize().width)
        ).print()

        return ExitCode.OK
    }
}