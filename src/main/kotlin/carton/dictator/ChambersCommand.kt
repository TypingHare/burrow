package burrow.carton.dictator

import burrow.carton.standard.Standard
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import java.util.concurrent.atomic.AtomicInteger

@CommandLine.Command(
    name = "init/chambers",
    description = ["Display the built chambers."]
)
class ChambersCommand(data: CommandData) : Command(data) {
    @CommandLine.Option(
        names = ["-a", "--all"],
        description = ["Display all chambers, including the chambers that have yet built."],
        defaultValue = "false"
    )
    var all = false

    override fun call(): Int {
        if (all) displayAllChambers() else displayBuiltChambers()

        return CommandLine.ExitCode.OK
    }

    private fun displayBuiltChambers() {
        val chambers = burrow.chamberShepherd.chambers
        val index = AtomicInteger(0)
        for ((name, chamber) in chambers.entries) {
            val config = chamber.config
            val alias = config.get<String>(Standard.ConfigKey.ALIAS)
            val description = config.get<String>(Standard.ConfigKey.DESCRIPTION)

            if (name == alias) {
                stdout.println("[$index] $name: $description")
            } else {
                stdout.println("[$index] $name($alias): $description")
            }

            index.incrementAndGet()
        }
    }

    private fun displayAllChambers() {

    }
}