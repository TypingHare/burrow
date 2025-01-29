package burrow.client

import burrow.carton.server.Endpoint
import burrow.kernel.stream.StateWriterController
import burrow.kernel.stream.state.InputState
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.ExitCode
import java.io.IOException
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
        val stateWriterController = StateWriterController(outputStream)
        stateWriterController.getPrintWriter(InputState.SESSION_CONTEXT).apply {
            println(
                Command.SessionContextKey.WORKING_DIRECTORY + " = " + System.getProperty(
                    "user.dir"
                )
            )
            println(
                Command.SessionContextKey.TERMINAL_SIZE + " = " + terminalSize
            )
        }
        stateWriterController.getPrintWriter(InputState.COMMAND).apply {
            println(command)
        }

        return processInputStreamForExitCode(inputStream, stateWriterController)
    }

    @Throws(IOException::class)
    override fun executeCommand(args: Array<String>): Int =
        executeCommand(getOriginalCommand(args))

    override fun close() {
        StateWriterController(clientSocket.getOutputStream()).getPrintWriter(
            InputState.SOCKET_CLOSE
        ).apply { println("-1") }

        clientSocket.close()
    }
}

fun getOriginalCommand(args: Array<String>): String {
    return args.joinToString(" ") { arg ->
        if (arg.contains(" ")) "\"$arg\"" else arg
    }
}