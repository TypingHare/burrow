package burrow.carton.server.command

import burrow.carton.server.Server
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = "server.start",
    header = ["Starts a Burrow service."]
)
class ServerStartCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        try {
            use(Server::class).start()
        } catch (ex: Exception) {
            val port = use(Server::class).getEndPoint().port
            stderr.println("Failed to start the server. Check if the port is allowed to listen on: $port")

            throw ex
        }

        return ExitCode.OK
    }
}