package burrow.server

import burrow.kernel.Burrow
import burrow.kernel.buildBurrow
import burrow.kernel.command.Environment
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket


class BurrowServer(val burrow: Burrow) : Closeable {
    private val logger = LoggerFactory.getLogger(BurrowServer::class.java)
    private val serverSocket = ServerSocket(Standard.PORT)

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("slf4j.internal.verbosity", "WARN")

            val burrowServer = BurrowServer(buildBurrow())
            Runtime.getRuntime()
                .addShutdownHook(Thread { burrowServer.close() })
            burrowServer.listen()
        }
    }

    fun listen() {
        while (true) {
            serverSocket.accept()?.let {
                val thread = Thread({ receive(it) }, "thd0")
                thread.start()
            }
        }
    }

    override fun close() {
        burrow.destroy()
    }

    private fun receive(client: Socket) {
        val remoteSocketAddress = client.remoteSocketAddress
        logger.info("Connected to $remoteSocketAddress")

        val inputStream = client.getInputStream()
        val input = BufferedReader(InputStreamReader(inputStream))
        var command: String?
        while (input.readLine().also { command = it } != null) {
            logger.info("Received command from $remoteSocketAddress: $command")
            processCommand(burrow, command!!, client.getOutputStream())
        }

        logger.info("Client disconnected: $remoteSocketAddress")
    }

    private fun processCommand(
        burrow: Burrow,
        commandString: String,
        outputStream: OutputStream
    ) {
        val args = DefaultParser().parse(
            Options(),
            commandString.split(" ").toTypedArray(),
            true
        ).args
        burrow.parse(args, Environment(outputStream, "~", 80))
    }

    object Standard {
        const val PORT = 4710
    }
}