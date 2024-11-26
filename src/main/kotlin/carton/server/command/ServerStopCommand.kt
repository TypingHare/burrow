package burrow.carton.server.command

import burrow.carton.server.Server
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode

@CommandLine.Command(
    name = "server.stop",
    description = ["Stops Burrow server."],
)
class ServerStopCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(Server::class).stop()
        return ExitCode.OK
    }
}