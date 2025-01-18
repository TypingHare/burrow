package burrow.carton.clutter.command

import burrow.carton.clutter.Clutter
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "carton.list",
    header = ["Displays all loaded cartons."]
)
class CartonListCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val clutter = use(Clutter::class)
        warehouse.cartons.values.joinToString("\n") { carton ->
            buildString {
                appendLine(clutter.stripBurrowPath(carton.path.toString()))
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