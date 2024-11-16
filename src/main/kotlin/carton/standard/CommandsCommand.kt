package burrow.carton.standard

import burrow.kernel.command.Command
import burrow.kernel.command.CommandClass
import burrow.kernel.command.CommandData
import burrow.kernel.furnishing.Furnishing
import picocli.CommandLine

@CommandLine.Command(
    name = "commands",
    description = [
        "Displays available commands for each furnishing in the system."
    ],
)
class CommandsCommand(data: CommandData) : Command(data) {
    @CommandLine.Option(
        names = ["-a", "--all"],
        description = [
            "Displays commands for all furnishings, including nested " +
                    "furnishings."
        ],
        defaultValue = "false"
    )
    var all = false

    override fun call(): Int {
        val furnishingsCommands =
            if (all) getAllFurnishingsCommandClasses()
            else getTopLevelFurnishingsCommandClasses()

        printFurnishingsCommands(furnishingsCommands)

        return CommandLine.ExitCode.OK
    }

    private fun getTopLevelFurnishingsCommandClasses(): FurnishingsCommandClasses {
        val map = mutableMapOf<Furnishing, List<CommandClass>>()
        chamber.renovator.dependencyTree.root.children.forEach {
            val furnishing = it.element ?: return@forEach
            map[furnishing] = furnishing.commands.toList()
        }

        return map
    }

    private fun getAllFurnishingsCommandClasses(): FurnishingsCommandClasses {
        val map = mutableMapOf<Furnishing, List<CommandClass>>()
        chamber.renovator.furnishings.values.forEach {
            map[it] = it.commands.toList()
        }

        return map
    }

    private fun printFurnishingsCommands(
        furnishingCommandClasses: FurnishingsCommandClasses
    ) {
        for ((furnishing, commandClasses) in furnishingCommandClasses) {
            if (commandClasses.isEmpty()) {
                continue
            }

            stdout.println(furnishing.getLabel())
            for (commandClass in commandClasses) {
                val name = extractName(commandClass)
                val description = extractDescription(commandClass)
                stdout.println("    ${name.padEnd(15)} $description")
            }

            stdout.println()
        }
    }
}

typealias FurnishingsCommandClasses = Map<Furnishing, List<CommandClass>>