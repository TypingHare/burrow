package burrow.carton.dictator.command

import burrow.carton.dictator.Dictator
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "chamber.exist",
    header = ["Checks if a chamber is built."]
)
class ChamberExistCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The name of the chamber to check."]
    )
    private var chamberName = ""

    @Option(
        names = ["--quiet", "-q"],
        description = ["Does not display strings."]
    )
    private var shouldBeQuiet = false

    override fun call(): Int {
        val dictator = use(Dictator::class)
        val existing = chamberName in dictator.chamberInfoMap.keys

        if (!shouldBeQuiet) {
            when (existing) {
                true -> stdout.println("Chamber exists: $chamberName")
                false -> stderr.println("Chamber does not exist: $chamberName")
            }
        }

        return if (existing) ExitCode.OK else ExitCode.USAGE
    }
}