package burrow.carton.standard.command

import burrow.carton.standard.Standard
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "config.get",
    description = ["Gets a config value."]
)
class ConfigGetCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The key."]
    )
    private var key = ""

    override fun call(): Int {
        val value = config.entries[key]
        if (value == null) {
            stderr.println("Key is not allowed: $key")
            return ExitCode.USAGE
        }

        val coloredValue =
            palette.color(value.toString(), Standard.Highlights.CONFIG_VALUE)
        stdout.println(coloredValue)

        return ExitCode.OK
    }
}