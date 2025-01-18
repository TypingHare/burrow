package burrow.carton.dictator.command

import burrow.carton.dictator.Dictator
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "blueprint.exist",
    header = ["Checks if a blueprint exists."]
)
class BlueprintExistCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["The name of the chamber to check."]
    )
    private var blueprintName = ""

    @Option(
        names = ["--quiet", "-q"],
        description = ["Does not display strings."]
    )
    private var shouldBeQuiet = false

    override fun call(): Int {
        val dictator = use(Dictator::class)
        val existing = dictator.getBlueprintNames()
            .contains(blueprintName) || blueprintName == ChamberShepherd.ROOT_CHAMBER_NAME

        if (!shouldBeQuiet) {
            when (existing) {
                true -> stdout.println("Blueprint exists: $blueprintName")
                false -> stderr.println("Blueprint does not exist: $blueprintName")
            }
        }

        return if (existing) ExitCode.OK else ExitCode.USAGE
    }
}