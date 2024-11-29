package burrow.carton.standard.command

import burrow.carton.standard.Standard
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Option

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
            stdout.println("Rebuilding chamber with configuration being preserved...")
            use(Standard::class).rebuildChamberPreservingConfig(stderr)
            stdout.println("Rebuilt successfully!")

            return ExitCode.OK
        }

        stdout.println("Rebuilding chamber...")
        chamber.rebuild()
        stdout.println("Rebuilt successfully!")

        return ExitCode.OK
    }
}