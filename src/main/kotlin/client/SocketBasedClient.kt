package burrow.client

import picocli.CommandLine.ExitCode
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import kotlin.system.exitProcess

class SocketBasedClient(host: String, port: Int) : Client() {
    private val clientSocket: Socket

    init {
        try {
            clientSocket = Socket(host, port)
        } catch (ex: IOException) {
            System.err.println("Failed to connect to Burrow server ($host:$port)!")
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
        writer.println(getWorkingDirectory())
        writer.println(terminalSize.toString())

        return processInputStreamForExitCode(inputStream)
    }

    override fun executeCommand(args: Array<String>): Int {
        return executeCommand(getOriginalCommand(args))
    }

    override fun close() {
        clientSocket.close()
    }
}