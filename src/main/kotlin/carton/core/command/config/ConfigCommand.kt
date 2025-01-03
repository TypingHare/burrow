package burrow.carton.core.command.config

import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "config",
    header = ["Displays chamber configurations."]
)
class ConfigCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val table = mutableListOf<List<String>>().apply {
            config.entries.forEach { (name, value) ->
                add(listOf(name, value.toString()))
            }
        }
        TablePrinter(
            stdout,
            TablePrinterContext(table, getTerminalWidth())
        ).print()

        return ExitCode.OK
    }
}