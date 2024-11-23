package burrow.carton.standard.command

import burrow.carton.standard.Standard
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.*

@CommandLine.Command(
    name = "chamber.rebuild",
    description = ["Rebuilds this chamber."]
)
class ChamberRebuildCommand(data: CommandData) : Command(data) {
    @Option(
        names = ["--preserve-config", "-c"]
    )
    private var shouldPreserveConfig = false

    override fun call(): Int {
        if (shouldPreserveConfig) {
            use(Standard::class).rebuildChamberPreservingConfig(stderr)

            return ExitCode.OK
        }

        chamber.rebuild()

        return ExitCode.OK
    }
}