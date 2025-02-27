package burrow.carton.server

import burrow.client.CommandLexer
import burrow.kernel.Burrow
import burrow.kernel.stream.StateBufferReader
import burrow.kernel.stream.state.InputState
import burrow.kernel.terminal.Environment
import org.slf4j.Logger
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

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
    private var serverSocket: ServerSocket? = null
    private val threadPool = Executors.newFixedThreadPool(5)

    override fun listen() {
        try {
            serverSocket = ServerSocket(endpoint.port)
        } catch (ex: Exception) {
            close()
            throw ex
        }

        logger.info("Socket service is listening on $endpoint")

        isRunning.set(true)
        while (isRunning.get()) {
            val clientSocket = serverSocket!!.accept()
            submitToThreadPool(clientSocket)
        }

        serverSocket?.close()
        logger.info("Socket service terminated")
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

        while (!client.isClosed) {
            try {
                receiveNextCommand(client, stateBufferReader).let {
                    if (it) client.close()
                }
            } catch (ex: IOException) {
                logger.error("Error reading from socket", ex)
                break
            }
        }

        logger.info("Client disconnected: $remoteSocketAddress")
    }

    override fun close() {
        isRunning.set(false)
        shutdown()
        super.close()
    }

    private fun submitToThreadPool(clientSocket: Socket) {
        threadPool.submit {
            try {
                receive(clientSocket)
            } catch (ex: Exception) {
                logger.error("Error handling client: ${ex.message}", ex)
            } finally {
                clientSocket.close()
            }
        }
    }

    private fun shutdown() {
        logger.info("Shutting down thread pool...")
        threadPool.shutdownNow()
    }

    private fun receiveNextCommand(
        client: Socket,
        reader: BufferedReader
    ): Boolean {
        val stateBufferReader =
            StateBufferReader(reader, InputState.SESSION_CONTEXT)
        val sessionContext = mutableMapOf<String, String>()
        val environment = Environment(
            client.getInputStream(),
            client.getOutputStream(),
            sessionContext
        )

        val isClosed = AtomicBoolean(false)
        stateBufferReader.readUntilNull { line, state, stopSignal ->
            if (client.isClosed) {
                stopSignal.set(true)
            }

            when (state) {
                InputState.COMMAND -> {
                    logger.debug("Command received: $line")
                    val args = CommandLexer.tokenizeCommandString(line)
                    environment.stateBufferReader = stateBufferReader
                    burrow.parse(args, environment)
                    stopSignal.set(true)
                }
                InputState.SESSION_CONTEXT -> {
                    logger.debug(line)
                    val index = line.indexOfFirst { it == '=' }
                    val key = line.substring(0, index)
                    val value = line.substring(index + 1)
                    sessionContext[key.trim()] = value.trim()
                }
                InputState.SOCKET_CLOSE -> {
                    isClosed.set(true)
                }
            }
        }

        return isClosed.get()
    }

    companion object {
        const val SERVICE_LOCK_FILE = "socket-service.lock"
    }
}