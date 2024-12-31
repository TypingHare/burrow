package burrow.carton.server.command

import burrow.carton.server.Server
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode

@CommandLine.Command(
    name = "server.start",
    description = ["Starts a Burrow service."]
)
class ServerStartCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(Server::class).start()
        return ExitCode.OK
    }
}