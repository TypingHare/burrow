package burrow.carton.server

import burrow.carton.server.command.ServerStartCommand
import burrow.carton.server.command.ServerStopCommand
import burrow.kernel.Burrow
import burrow.kernel.config.Config
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Furniture
import org.slf4j.LoggerFactory

@Furniture(
    version = Burrow.VERSION,
    description = "Hosts a burrow service.",
    type = Furniture.Type.ROOT
)
class Server(renovator: Renovator) : Furnishing(renovator) {
    private var service: Service? = null

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

    private fun getEndPoint() = Endpoint(
        config.getNotNull(ConfigKey.HOST),
        config.getNotNull(ConfigKey.PORT)
    )

    fun start() {
        val logger = LoggerFactory.getLogger(javaClass)
        service = SocketService(burrow, logger, getEndPoint()).apply {
            listen()
        }
    }

    fun stop() {
        service?.close()
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