package burrow.carton.core.command

import burrow.kernel.terminal.*

@BurrowCommand(
    name = "config.get",
    description = ["Get the config value associated with a specific key."]
)
class ConfigGetCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The key associated with the value"]
    )
    private var key = ""

    override fun call(): Int {
        val value = config.entries[key]
        if (value == null) {
            stderr.println("Key is not allowed: $key")
            return ExitCode.USAGE
        }

        stdout.println(value)

        return ExitCode.OK
    }
}