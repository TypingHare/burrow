package burrow.carton.core.command.config

import burrow.carton.core.Core
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "config",
    header = ["Displays chamber configurations."]
)
class ConfigCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The name of the furnishing."],
        defaultValue = ""
    )
    private var furnishingName = ""

    override fun call(): Int {
        val configMap = when (furnishingName) {
            "" -> config.entries
            else -> getFurnishingConfig()
        }

        val sortedConfigMap = configMap
            .toSortedMap()
            .mapValues { it.value.toString() }
        val table = mutableListOf<List<String>>().apply {
            sortedConfigMap.forEach { (key, value) -> add(listOf(key, value)) }
        }

        val context = TablePrinterContext(table, getTerminalWidth())
        TablePrinter(stdout, context).print()

        return ExitCode.OK
    }

    private fun getFurnishingConfig(): Map<String, Any?> {
        val furnishingId = renovator.getUniqueFurnishingId(furnishingName)
        val furnishing = renovator.getFurnishing(furnishingId)!!
        val configKeys = use(Core::class).getFurnishingConfigKeys(furnishing)

        return config.entries.filterKeys { configKeys.contains(it) }
    }
}