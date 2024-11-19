package burrow.carton.server.command

import burrow.carton.server.Server
import burrow.carton.server.SocketService
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.ExitCode

@CommandLine.Command(
    name = "server.start",
    description = ["Starts a Burrow server."]
)
class ServerStartCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val server = use(Server::class)
        SocketService(burrow, server.getHost(), server.getPort()).listen()

        return ExitCode.OK
    }
}