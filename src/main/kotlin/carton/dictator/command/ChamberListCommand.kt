package burrow.carton.dictator.command

import burrow.carton.dictator.ChamberInfo
import burrow.carton.dictator.Dictator
import burrow.kernel.Burrow
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger

@CommandLine.Command(
    name = "chamber.list",
    description = ["Display the built chambers."]
)
class ChamberListCommand(data: CommandData) : Command(data) {
    @CommandLine.Option(
        names = ["-a", "--all"],
        description = ["Display all chambers, including the chambers that have yet built."],
        defaultValue = "false"
    )
    var shouldDisplayAll = false

    override fun call(): Int {
        val dictator = use(Dictator::class)
        val chamberInfoList = if (shouldDisplayAll) {
            dictator.getAllChamberInfo()
        } else {
            dictator.chamberInfoMap.values.toList()
        }

        val index = AtomicInteger(0)
        chamberInfoList.forEach {
            displayChamberInfo(stdout, it, index.getAndIncrement())
        }

        return CommandLine.ExitCode.OK
    }

    private fun displayChamberInfo(
        writer: PrintWriter,
        chamberInfo: ChamberInfo,
        index: Int
    ) {
        val (name, alias, description) = chamberInfo
        val coloredName = palette.color(name, Burrow.Highlights.CHAMBER)
        val coloredAlias = palette.color(alias, Burrow.Highlights.CHAMBER)
        if (name == alias) {
            writer.println("[$index] $coloredName  $description")
        } else {
            writer.println("[$index] $coloredAlias($coloredName)  $description")
        }
    }
}