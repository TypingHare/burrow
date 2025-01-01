package burrow.client

import burrow.carton.server.Endpoint
import burrow.kernel.terminal.ExitCode
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import kotlin.system.exitProcess

class SocketBasedClient(endpoint: Endpoint) : Client() {
    private val clientSocket: Socket

    init {
        try {
            clientSocket = Socket(endpoint.host, endpoint.port)
        } catch (ex: Exception) {
            System.err.println("Failed to connect to Burrow server ($endpoint)!")
            exitProcess(ExitCode.SOFTWARE)
        }
    }

    @Throws(IOException::class)
    override fun executeCommand(command: String): Int {
        val inputStream = clientSocket.getInputStream()
        val outputStream = clientSocket.getOutputStream()

        val terminalSize = getTerminalSize()
        val writer = PrintWriter(outputStream, true)
        writer.println(command)
        writer.println(System.getProperty("user.dir"))
        writer.println(terminalSize.toString())

        return processInputStreamForExitCode(inputStream)
    }

    @Throws(IOException::class)
    override fun executeCommand(args: Array<String>): Int =
        executeCommand(getOriginalCommand(args))

    override fun close() {
        clientSocket.close()
    }
}

fun getOriginalCommand(args: Array<String>): String {
    return args.joinToString(" ") { arg ->
        if (arg.contains(" ")) "\"$arg\"" else arg
    }
}