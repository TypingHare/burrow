package burrow.carton.server.command

import burrow.carton.server.Server
import burrow.kernel.terminal.*

@BurrowCommand(
    name = "server.stop",
    header = ["Stops the Burrow service."]
)
class ServerStopCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        use(Server::class).stop()
        return ExitCode.OK
    }
}