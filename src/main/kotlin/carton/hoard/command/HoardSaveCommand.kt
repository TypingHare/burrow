package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "hoard.save",
    description = ["Saves the hoard."]
)
class HoardSaveCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(Hoard::class).save()
        return ExitCode.OK
    }
}