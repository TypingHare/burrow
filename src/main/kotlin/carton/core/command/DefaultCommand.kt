package burrow.carton.core.command

import burrow.carton.core.Core
import burrow.carton.core.command.furnishing.FurnishingListCommand
import burrow.kernel.Burrow
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.extractType
import burrow.kernel.furniture.extractVersion
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode
import picocli.CommandLine

@BurrowCommand(
    name = Core.DEFAULT_COMMAND_NAME,
    header = [
        "The default command executed when no command is specified."
    ]
)
class DefaultCommand(data: CommandData) : Command(data) {
    @CommandLine.Option(
        names = ["--version", "-v"],
        description = [
            "Prints the version of the chamber. The version of the chamber " +
                    "is the versions of the main furnishings."
        ]
    )
    private var showVersion = false

    @CommandLine.Option(
        names = ["--help", "-h"],
        description = ["Displays the help information."],
    )
    private var showHelp = false

    override fun call(): Int {
        if (showVersion) return displayVersion()
        if (showHelp) return displayHelp()

        stderr.println("No such command: ")

        return displayHelp()
    }

    private fun displayVersion(): Int {
        val chamberName = getChamberName()
        val nameVersionMap = renovator.furnishings.values
            .filter { extractType(it::class) == Furniture.Type.MAIN }
            .associate { it.javaClass.simpleName to extractVersion(it::class) }

        when (nameVersionMap.size) {
            0 -> stdout.println("$chamberName  ${Burrow.VERSION}")
            else -> {
                val versionString = nameVersionMap.map { (name, version) ->
                    "$name=$version"
                }.joinToString(" | ")
                stdout.println("$chamberName  $versionString")
            }
        }

        return ExitCode.OK
    }

    private fun getChamberName(): String {
        if (chamber.name == ChamberShepherd.ROOT_CHAMBER_NAME) {
            return "Burrow"
        }

        return chamber.name
    }

    private fun displayHelp(): Int {
        displayVersion()

        // Description
        val description = config.getNotNull<String>(Core.ConfigKey.DESCRIPTION)
        stdout.println()
        stdout.println("[Description]")
        stdout.println(description)

        // Involved cartons
        val furnishingIds = renovator.furnishings.keys
        val burrowPathString = burrow.getPath().toString()
        val cartonPathStringSet = furnishingIds.map { furnishingId ->
            val absolutePathString =
                warehouse.furnishingIdToCarton[furnishingId]!!.path.toString()
            when (absolutePathString.startsWith(burrowPathString)) {
                true -> absolutePathString.substring(burrowPathString.length + 1)
                false -> absolutePathString
            }
        }.toSet()

        stdout.println()
        stdout.println("[Cartons]")
        cartonPathStringSet.sorted().map(stdout::println)

        // Installed furnishings
        stdout.println()
        stdout.println("[Furnishings]")
        dispatch(FurnishingListCommand::class)

        return ExitCode.OK
    }
}