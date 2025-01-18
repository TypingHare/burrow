package burrow.carton.core.command.config

import burrow.kernel.terminal.*

@BurrowCommand(
    name = "config.get",
    header = ["Gets the config value associated with a specific key."]
)
class ConfigGetCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The key associated with the value"]
    )
    private var key = ""

    override fun call(): Int {
        stdout.println(config.getNotNull<Any>(key))
        return ExitCode.OK
    }
}