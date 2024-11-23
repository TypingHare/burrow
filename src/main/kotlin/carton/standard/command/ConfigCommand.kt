package burrow.carton.standard.command

import burrow.carton.standard.Standard
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode

@CommandLine.Command(
    name = "config",
    description = ["Displays chamber configurations."]
)
class ConfigCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val entries = config.entries
        entries.forEach { (name, value) ->
            val coloredName =
                palette.color(name, Standard.Highlights.CONFIG_KEY)
            val coloredValue =
                palette.color("\"$value\"", Standard.Highlights.CONFIG_VALUE)
            stdout.println("$coloredName -> $coloredValue")
        }

        return ExitCode.OK
    }
}