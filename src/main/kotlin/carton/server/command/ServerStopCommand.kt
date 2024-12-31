package burrow.carton.server.command

import burrow.carton.server.Server
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode
import picocli.CommandLine

@CommandLine.Command(
    name = "server.stop",
    description = ["Stops the Burrow service."]
)
class ServerStopCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(Server::class).stop()
        return ExitCode.OK
    }
}