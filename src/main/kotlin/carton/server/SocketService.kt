package burrow.carton.server

import burrow.client.CommandLexer
import burrow.kernel.Burrow
import burrow.kernel.stream.StateBufferReader
import burrow.kernel.stream.state.InputState
import burrow.kernel.terminal.Environment
import org.slf4j.Logger
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import kotlin.math.log

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
        val stateBufferReader =
            StateBufferReader(
                InputStreamReader(inputStream),
                InputState.SESSION_CONTEXT
            )

        while (client.isConnected) {
            receiveNextCommand(client, stateBufferReader)
        }

        logger.info("Client disconnected: $remoteSocketAddress")
    }

    private fun receiveNextCommand(client: Socket, reader: BufferedReader) {
        val stateBufferReader =
            StateBufferReader(reader, InputState.SESSION_CONTEXT)
        val sessionContext = mutableMapOf<String, String>()
        val environment = Environment(
            client.getInputStream(),
            client.getOutputStream(),
            sessionContext
        )

        stateBufferReader.readUntilNull { line, state, stopSignal ->
            when (state) {
                InputState.COMMAND -> {
                    logger.info("Command received: $line")

                    val args = CommandLexer.tokenizeCommandString(line)
                    burrow.parse(args, environment)
                    stopSignal.set(true)
                }
                InputState.SESSION_CONTEXT -> {
                    logger.info(line)
                    val index = line.indexOfFirst { it == '=' }
                    val key = line.substring(0, index)
                    val value = line.substring(index + 1)
                    sessionContext[key.trim()] = value.trim()
                }
            }
        }
    }

    companion object {
        const val SERVICE_LOCK_FILE = "socket-service.lock"
    }
}