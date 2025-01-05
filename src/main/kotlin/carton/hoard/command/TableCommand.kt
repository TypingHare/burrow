package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "table",
    header = ["Display entries in the table form."]
)
class TableCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The ID to start with."],
        defaultValue = "1"
    )
    private var startId = 1

    @Parameters(
        index = "1",
        description = ["The number of the entries to display."],
        defaultValue = "-1"
    )
    private var length = -1

    @Option(
        names = ["--reverse", "-r"],
        description = ["Reverse."]
    )
    private var shouldReverse = false

    @Option(
        names = ["--keys", "-k"],
        description = ["A list of keys to be displayed."],
        defaultValue = ""
    )
    private var keysString: String = ""

    override fun call(): Int {
        val hoard = use(Hoard::class)
        val propertiesList = hoard.getAllEntries()
            .filter { it.id >= startId }
            .let { if (length in it.indices) it.subList(0, length) else it }
            .map { Pair(it.id, hoard.formatStore(it)) }
            .let { if (shouldReverse) it.reversed() else it }

        val propertyKeyList = when (keysString) {
            "" -> getPropertyKeys(propertiesList)
            else -> keysString.split(Hoard.KEY_DELIMITER).map { it.trim() }
        }
        val table = mutableListOf<List<String>>().apply {
            add(mutableListOf("ID").apply { addAll(propertyKeyList) })
        }
        for ((id, properties) in propertiesList) {
            val record = mutableListOf(id.toString())
            for (propertyKey in propertyKeyList) {
                record.add(properties[propertyKey] ?: "")
            }
            table.add(record)
        }

        TablePrinter(stdout, TablePrinterContext(table, getTerminalWidth()))
            .print()

        return ExitCode.OK
    }

    private fun getPropertyKeys(
        propertiesList: List<Pair<Int, Map<String, String>>>
    ): List<String> {
        // Collect the property keys
        val propertyKeySet = mutableSetOf<String>()
        for (properties in propertiesList) {
            properties.second.keys.forEach { propertyKeySet.add(it) }
        }

        return propertyKeySet.toList()
    }
}