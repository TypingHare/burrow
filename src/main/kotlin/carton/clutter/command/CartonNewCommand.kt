package burrow.carton.clutter.command

import burrow.carton.clutter.InvalidCartonNameException
import burrow.kernel.terminal.*
import kotlin.io.path.createDirectories

@BurrowCommand(
    name = "carton.new",
    header = ["Creates a new carton."]
)
class CartonNewCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["Carton's name; allows letters, numbers, and underscores."]
    )
    private var cartonName = ""

    override fun call(): Int {
        if (!cartonName.matches(Regex("[A-Za-z0-9_]+"))) {
            throw InvalidCartonNameException(cartonName)
        }

        getWorkingDirectory().resolve(cartonName).createDirectories()

        return ExitCode.OK
    }
}

