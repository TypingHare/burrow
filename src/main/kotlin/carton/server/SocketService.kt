package burrow.carton.server

import burrow.kernel.Burrow
import org.slf4j.Logger
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import kotlin.io.path.deleteIfExists

class SocketService(
    burrow: Burrow,
    logger: Logger,
    endpoint: Endpoint
) : Service(
    burrow,
    logger,
    endpoint,
    burrow.getPath().resolve(SERVICE_LOCK_FILE)
), Closeable {
    companion object {
        const val SERVICE_LOCK_FILE = "socket-service.lock"
    }

    private var serverSocket = ServerSocket(endpoint.port)

    override fun listen() {
        val serverSocket = serverSocket
        logger.info("Server listening on $endpoint")
        while (true) {
            serverSocket.accept()?.let {
                Thread({ receive(it) }, "thd0").start()
            }
        }
    }

    override fun receive(client: Socket) {
        val remoteSocketAddress = client.remoteSocketAddress
        logger.info("Connected to $remoteSocketAddress")

        val inputStream = client.getInputStream()
        val reader = BufferedReader(InputStreamReader(inputStream))
//        receiveNextCommand(client, reader)

        logger.info("Client disconnected: $remoteSocketAddress")
    }
}