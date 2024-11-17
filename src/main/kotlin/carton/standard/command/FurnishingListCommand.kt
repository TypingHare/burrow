package burrow.carton.standard.command

import burrow.carton.standard.Standard
import burrow.carton.standard.printer.*
import burrow.kernel.Burrow
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine

@CommandLine.Command(
    name = "furnishing.list",
    description = [
        "Display a complete list of all available furnishings instead of the " +
                "dependency tree."
    ]
)
class FurnishingListCommand(data: CommandData) : Command(data) {
    @CommandLine.Option(
        names = ["-t", "--tree"],
        description = [
            "Displays furnishings as a dependency tree."
        ]
    )
    var shouldDisplayTree = false

    @CommandLine.Option(
        names = ["-a", "--all"],
        description = [
            "Displays all available furnishings."
        ]
    )
    var shouldDisplayAll = false

    @CommandLine.Option(
        names = ["-i", "--id"],
        description = [
            "Displays the ID of each furnishing."
        ]
    )
    var shouldDisplayId = false

    override fun call(): Int {
        val standard = use(Standard::class)
        if (shouldDisplayTree) {
            val dependencyTree =
                if (shouldDisplayAll) standard.getCompleteFurnishingClassDependencyTree()
                else standard.getFurnishingClassDependencyTree()
            val context = FurnishingClassDependencyTreePrintContext(
                dependencyTree,
                palette,
                Burrow.Highlights.FURNISHING,
            )
            context.shouldPrintFurnishingId = shouldDisplayId
            val task = FurnishingClassDependencyTreePrintTask(stdout, context)
            task.print()
        } else {
            if (shouldDisplayAll) {
                val list = standard.getAvailableFurnishingClasses().stream()
                    .sorted { a, b ->
                        a.java.name.compareTo(b.java.name)
                    }.toList()
                val context = CompleteFurnishingClassesPrintContext(
                    list,
                    standard.getFurnishingClasses(),
                    palette,
                    Standard.Highlights.DEFAULT_FURNISHING,
                    Standard.Highlights.INSTALLED_FURNISHING
                )
                context.shouldPrintFurnishingId = shouldDisplayId
                CompleteFurnishingClassesPrintTask(stdout, context).print()
            } else {
                val list = standard.getAvailableFurnishingClasses().toList()
                val context = FurnishingClassesPrintContext(
                    list,
                    palette,
                    Standard.Highlights.INSTALLED_FURNISHING
                )
                context.shouldPrintFurnishingId = shouldDisplayId
                FurnishingClassesPrintTask(stdout, context).print()
            }
        }

        return CommandLine.ExitCode.OK
    }
}