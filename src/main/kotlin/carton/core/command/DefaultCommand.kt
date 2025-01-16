package burrow.carton.core.command

import burrow.carton.core.Core
import burrow.carton.core.command.furnishing.FurnishingListCommand
import burrow.kernel.Burrow
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.extractType
import burrow.kernel.furniture.extractVersion
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode
import picocli.CommandLine
import java.nio.file.Path

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
        if (showVersion) {
            return displayVersion()
        }

        if (showHelp) {
            return displayHelp()
        }


        return ExitCode.OK
    }

    private fun displayVersion(): Int {
        val chamberName = chamber.name
        val idVersionMap = renovator.furnishings.values
            .filter { extractType(it::class) == Furniture.Type.MAIN }
            .associate { it.javaClass.name to extractVersion(it::class) }

        when (idVersionMap.size) {
            0 -> stdout.println("$chamberName  ${Burrow.VERSION}")
            else -> {
                val versionString = idVersionMap.map { (id, version) ->
                    "$id=$version"
                }.joinToString(" | ")
                stdout.println("$chamberName  $versionString")
            }
        }

        stdout.println()

        return ExitCode.OK
    }

    private fun displayHelp(): Int {
        val description = config.getNotNull<String>(Core.ConfigKey.DESCRIPTION)
        stdout.println("${chamber.name} - $description")

        // Involved cartons
        val furnishingIds = renovator.furnishings.keys
        val cartonPathSet = mutableSetOf<Path>().apply {
            for (furnishingId in furnishingIds) {
                this.add(warehouse.furnishingIdToCarton[furnishingId]!!.path)
            }
        }

        stdout.println()
        stdout.println("[Cartons]")
        cartonPathSet.map { it.toString() }.sorted().map(stderr::println)

        stdout.println()
        stdout.println("[Furnishings]")
        dispatch(FurnishingListCommand::class)

        return ExitCode.OK
    }
}