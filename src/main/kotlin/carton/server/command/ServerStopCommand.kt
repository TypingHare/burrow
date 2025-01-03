package burrow.carton.server.command

import burrow.carton.server.Server
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode

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