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
        use(Server::class).start()
        return ExitCode.OK
    }
}