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

    override fun call(): Int {
        val hoard = use(Hoard::class)
        val propertiesList = hoard.getAllEntries()
            .filter { it.id >= startId }
            .let { if (length in it.indices) it.subList(0, length) else it }
            .map { Pair(it.id, hoard.formatStore(it)) }

        // Collect the property keys
        val propertyKeySet = mutableSetOf<String>()
        for (properties in propertiesList) {
            properties.second.keys.forEach { propertyKeySet.add(it) }
        }

        if (propertyKeySet.size > 10) {
            stderr.println("There are more than 10 property keys. The table would be too big!")
            return ExitCode.USAGE
        }

        // Create the table
        val propertyKeyList = propertyKeySet.toList()
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
}