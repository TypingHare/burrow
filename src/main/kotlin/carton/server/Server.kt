package burrow.carton.server

import burrow.carton.server.command.ServerStartCommand
import burrow.carton.server.command.ServerStopCommand
import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.config.Config
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.annotation.Furniture

@Furniture(
    version = Burrow.VERSION.NAME,
    description = "Allows Burrow to host a server.",
    type = Furniture.Type.ROOT
)
class Server(chamber: Chamber) : Furnishing(chamber) {
    private var host = Default.HOST
    private var port = Default.PORT
    private var socketService: SocketService? = null

    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.HOST)
        config.addKey(ConfigKey.PORT, { it.toInt() }, { it.toString() })
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.HOST, Default.HOST)
        config.setIfAbsent(ConfigKey.PORT, Default.PORT)
    }

    override fun assemble() {
        registerCommand(ServerStartCommand::class)
        registerCommand(ServerStopCommand::class)
    }

    override fun launch() {
        host = config.get<String>(ConfigKey.HOST)!!
        port = config.get<Int>(ConfigKey.PORT)!!
    }

    fun start() {
        socketService = SocketService(burrow, host, port)
        socketService!!.listen()
    }

    fun stop() {
        if (socketService != null) {
            socketService!!.close()
        }
    }

    object ConfigKey {
        const val HOST = "server.host"
        const val PORT = "server.port"
    }

    object Default {
        const val HOST = "localhost"
        const val PORT = 4710
    }
}