package burrow.carton.server.command

import burrow.carton.server.Server
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
        use(Server::class).start()

        return ExitCode.OK
    }
}