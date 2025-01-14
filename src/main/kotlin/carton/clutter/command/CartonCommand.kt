package burrow.carton.clutter.command

import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "carton",
    header = ["Displays all cartons loaded."]
)
class CartonCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        warehouse.cartons.values.joinToString("\n\n") { carton ->
            buildString {
                appendLine(carton.path)
                carton.properties.forEach { (key, value) ->
                    appendLine("  ~ $key = $value")
                }
                carton.furnishingClasses.forEach {
                    appendLine("  - ${it.java.name}")
                }
            }
        }.let(stdout::println)

        return ExitCode.OK
    }
}