package burrow.carton.server

import burrow.common.CommandLexer
import burrow.kernel.Burrow
import burrow.kernel.command.Environment
import burrow.kernel.command.TerminalSize
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import kotlin.io.path.deleteIfExists

class SocketService(
    private val burrow: Burrow,
    private val host: String,
    private val port: Int
) : Closeable {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(SocketService::class.java)
    }

    private var serverSocket: ServerSocket? = null
    private val serviceLockPath = Burrow.getRootPath().resolve("service.lock")

    init {
        serverSocket = ServerSocket(port)
        writeServiceLockFile()
    }

    fun listen() {
        val serverSocket = serverSocket!!
        logger.info("Server listening on $host:$port")
        while (true) {
            serverSocket.accept()?.let {
                Thread({ receive(it) }, "thd0").start()
            }
        }
    }

    override fun close() {
        if (serverSocket != null) {
            serverSocket!!.close()
        }
        burrow.destroy()
        serviceLockPath.deleteIfExists()
    }

    private fun receive(client: Socket) {
        val remoteSocketAddress = client.remoteSocketAddress
        logger.info("Connected to $remoteSocketAddress")

        val inputStream = client.getInputStream()
        val reader = BufferedReader(InputStreamReader(inputStream))
        receiveNextCommand(client, reader)

        logger.info("Client disconnected: $remoteSocketAddress")
    }

    private fun receiveNextCommand(
        client: Socket,
        reader: BufferedReader
    ) {
        val remoteSocketAddress = client.remoteSocketAddress
        var line: String
        while (reader.readLine().also { line = it } != null) {
            val command = line
            logger.info("Received command from $remoteSocketAddress: $command")

            val workingDirectory = reader.readLine()
            val (width, height) = reader.readLine().split(" ")
                .map { it.toInt() }
            val environment = Environment(
                client.getOutputStream(),
                workingDirectory,
                TerminalSize(width, height),
            )

            processCommand(command, environment)
        }
    }

    private fun writeServiceLockFile() {
        val string = "$host\n$port"
        Files.write(serviceLockPath, string.toByteArray())
    }

    private fun processCommand(
        commandString: String,
        environment: Environment
    ) {
        burrow.parse(
            CommandLexer.tokenizeCommandString(commandString).toTypedArray(),
            environment
        )
    }
}