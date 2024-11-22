package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode

@CommandLine.Command(
    name = "hoard.save",
    description = ["Saves the hoard."]
)
class HoardSaveCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val hoard = use(Hoard::class)
        hoard.saveToHoardFile(hoard.hoardFilePath)

        return ExitCode.OK
    }
}