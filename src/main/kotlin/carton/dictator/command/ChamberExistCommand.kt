package burrow.carton.dictator.command

import burrow.carton.dictator.Dictator
import burrow.kernel.Burrow
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.*

@CommandLine.Command(
    name = "chamber.exist",
    description = ["Checks if a chamber exists."]
)
class ChamberExistCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = [
            "The name of the chamber to check."
        ]
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
        names = ["--silent", "-s"],
        description = [
            "Does not display strings."
        ]
    )
    private var shouldBeSilent = false

    override fun call(): Int {
        val dictator = use(Dictator::class)
        if (checkBluePrint) {
            val chamberNameSet =
                dictator.getAllChamberDirs().map { it.name }.toSet()
            return satisfy(chamberName in chamberNameSet || chamberName == Burrow.Standard.ROOT_CHAMBER_NAME)
        }

        return satisfy(chamberName in dictator.chamberInfoMap.keys)
    }

    private fun satisfy(condition: Boolean): Int {
        if (!shouldBeSilent) {
            if (condition) {
                stdout.println("Chamber exists: $chamberName")
            } else {
                stderr.println("Chamber does not exist: $chamberName")
            }
        }

        return if (condition) ExitCode.OK else ExitCode.USAGE
    }
}