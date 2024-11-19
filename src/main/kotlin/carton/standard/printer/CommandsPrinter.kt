package burrow.carton.standard.printer

import burrow.carton.standard.FurnishingsCommandClasses
import burrow.carton.standard.Standard
import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.command.Command.Companion.extractDescription
import burrow.kernel.command.Command.Companion.extractName
import burrow.kernel.stream.ChamberBasedPrinter
import java.io.PrintWriter

abstract class CommandsPrinter(writer: PrintWriter, chamber: Chamber) :
    ChamberBasedPrinter(writer, chamber) {
    protected fun printFurnishingCommandClasses(
        furnishingCommandClasses: FurnishingsCommandClasses
    ) {
        for ((furnishing, commandClasses) in furnishingCommandClasses) {
            if (commandClasses.isEmpty()) {
                continue
            }

            val id = furnishing.getId()
            val label = furnishing.getLabel()
            val coloredLabel =
                chamber.palette.color(label, Burrow.Highlights.FURNISHING)
            writer.println("$coloredLabel ($id)")
            for (commandClass in commandClasses) {
                val name = extractName(commandClass)
                val description = extractDescription(commandClass)
                val coloredName =
                    chamber.palette.color(name, Burrow.Highlights.COMMAND)
                writer.println("  ${coloredName.padEnd(40)} $description")
            }

            writer.println()
        }
    }
}

class AllFurnishingsCommandsPrinter(writer: PrintWriter, chamber: Chamber) :
    CommandsPrinter(writer, chamber) {
    override fun print() {
        val furnishingClasses =
            chamber.use(Standard::class).getAllFurnishingsCommandClasses()
        printFurnishingCommandClasses(furnishingClasses)
    }
}

class TopLevelFurnishingsCommandsPrinter(
    writer: PrintWriter,
    chamber: Chamber
) : CommandsPrinter(writer, chamber) {
    override fun print() {
        val furnishingClasses =
            chamber.use(Standard::class).getTopLevelFurnishingsCommandClasses()
        printFurnishingCommandClasses(furnishingClasses)
    }
}