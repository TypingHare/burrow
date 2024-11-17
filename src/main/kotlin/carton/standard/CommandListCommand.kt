package burrow.carton.standard

import burrow.carton.standard.printer.AllFurnishingsCommandsPrinter
import burrow.carton.standard.printer.TopLevelFurnishingsCommandsPrinter
import burrow.kernel.command.Command
import burrow.kernel.command.CommandClass
import burrow.kernel.command.CommandData
import burrow.kernel.furnishing.Furnishing
import picocli.CommandLine

typealias FurnishingsCommandClasses = Map<Furnishing, List<CommandClass>>