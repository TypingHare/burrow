package burrow.carton.standard.command

import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.*

@CommandLine.Command(
    name = "config.set",
    description = ["Sets a value associated with a specific key."]
)
class ConfigSetCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The key."]
    )
    private var key = ""

    @Parameters(
        index = "1",
        description = ["The value to set."],
        defaultValue = ""
    )
    private var value = ""

    @Option(
        names = ["--reset", "-r"],
        description = ["Reset the config value."]
    )
    private var shouldReset = false

    override fun call(): Int {
        if (key !in config.entries.keys) {
            stderr.println("Key is not allowed: $key")
            return ExitCode.USAGE
        }

        val handler = config.itemHandlers[key]
        if (handler == null) {
            stderr.println("Handler not found: $key")
            return ExitCode.USAGE
        }

        if (shouldReset) {
            // Delete the key
            config.entries.remove(key)
            renovator.depTree.resolve { it.modifyConfig(config) }
        } else {
            config.set(key, handler.reader.read(value))
        }

        stdout.println("Rebuilding the chamber...")
        chamber.rebuild()
        stdout.println("Rebuilt successfully!")

        return ExitCode.OK
    }
}