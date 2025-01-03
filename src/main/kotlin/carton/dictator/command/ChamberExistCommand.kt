package burrow.carton.dictator.command

import burrow.carton.dictator.Dictator
import burrow.kernel.chamber.ChamberShepherd
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
        names = ["--blueprint", "-b"],
        description = [
            "Checks if blueprint exists instead of being built."
        ]
    )
    private var checkBluePrint = false

    @Option(
        names = ["--quiet", "-q"],
        description = ["Does not display strings."]
    )
    private var shouldBeQuiet = false

    override fun call(): Int {
        val dictator = use(Dictator::class)
        if (checkBluePrint) {
            val chamberNames = dictator.getAllChamberNames()
            return satisfy(chamberNames.contains(chamberName) || chamberName == ChamberShepherd.ROOT_CHAMBER_NAME)
        }

        return satisfy(chamberName in dictator.chamberInfoMap.keys)
    }

    private fun satisfy(condition: Boolean): Int {
        if (!shouldBeQuiet) {
            if (condition) {
                stdout.println("Chamber exists: $chamberName")
            } else {
                stderr.println("Chamber does not exist: $chamberName")
            }
        }

        return if (condition) ExitCode.OK else ExitCode.USAGE
    }
}