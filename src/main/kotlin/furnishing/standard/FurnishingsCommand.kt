package burrow.furnishing.standard

import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import burrow.kernel.furnishing.Furnishing
import picocli.CommandLine
import java.util.concurrent.atomic.AtomicInteger

@CommandLine.Command(
    name = "furnishings",
    description = ["Display the furnishing dependency tree."]
)
class FurnishingsCommand(data: CommandData) : Command(data) {
    @CommandLine.Option(
        names = ["-a", "--all"],
        description = ["Display a list of all available furnishings."],
        defaultValue = "false"
    )
    var all = false

    override fun call(): Int {
        if (all) displayFurnishingList() else displayFurnishingDependencyTree()

        return CommandLine.ExitCode.OK
    }

    private fun displayFurnishingDependencyTree() {
        val dependencyTree = 0;
    }

    private fun displayFurnishingList() {
        val furnishingClasses = burrow.furnishingWarehouse.furnishingClasses
        val index = AtomicInteger(0)
        for (furnishingClass in furnishingClasses) {
            val id = Furnishing.extractId(furnishingClass)
            val label = Furnishing.extractLabel(furnishingClass)
            stdout.println("[${index.getAndIncrement()}] $label ($id)")
        }
    }
}