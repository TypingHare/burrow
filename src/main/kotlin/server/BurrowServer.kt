package burrow.server

import burrow.kernel.Burrow
import burrow.kernel.buildBurrow
import burrow.kernel.command.Environment
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class BurrowServer(val burrow: Burrow) {
    private val logger = LoggerFactory.getLogger(BurrowServer::class.java)
    private val serverSocket = ServerSocket(Standard.PORT)

    fun listen() {
        while (true) {
            serverSocket.accept()?.let {
                val thread = Thread({ receive(it) }, "0")
                thread.start()
            }
        }
    }

    private fun receive(client: Socket) {
        logger.info("Connected to ${client.remoteSocketAddress}")

        val inputStream = client.getInputStream()
        val input = BufferedReader(InputStreamReader(inputStream))
        var command: String?
        while (input.readLine().also { command = it } != null) {
            logger.info("Received command: $command")
            processCommand(burrow, command!!, client.getOutputStream())
        }

        logger.info("Client ${client.remoteSocketAddress} disconnected")
    }

    private fun processCommand(
        burrow: Burrow,
        command: String,
        outputStream: OutputStream
    ) {
        burrow.parse(command, Environment(outputStream, "~", 80))
    }

    object Standard {
        const val PORT = 4710
    }
}

fun main() {
    System.setProperty("slf4j.internal.verbosity", "WARN")
    BurrowServer(buildBurrow()).listen()
}