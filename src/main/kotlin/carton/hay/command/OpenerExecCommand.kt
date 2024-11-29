package burrow.carton.hay.command

import burrow.carton.cradle.ExecCommand
import burrow.carton.hay.Hay
import burrow.carton.hay.HayOpener
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.Parameters

@CommandLine.Command(
    name = "opener.exec",
    description = ["Executes an entry using the opener."]
)
class OpenerExecCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        paramLabel = "<relative-path>",
        description = ["The relative path of the entry."]
    )
    private var relativePath = ""

    override fun call(): Int {
        val entry = use(Hay::class).getEntry(relativePath)
        val opener = HayOpener.extractOpener(entry)
        val absolutePath = Hay.extractAbsolutePath(entry)
        val command = "$opener $absolutePath"

        return dispatch(ExecCommand::class, listOf(command))
    }
}