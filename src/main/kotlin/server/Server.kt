package burrow.server

import burrow.kernel.Burrow
import burrow.kernel.buildBurrow
import burrow.kernel.command.Environment
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

fun main() {
    System.setProperty("slf4j.internal.verbosity", "WARN")
    val server = ServerSocket(12345)
    println("Burrow server started. Listening on port 12345...")

    val burrow = buildBurrow()

    while (true) {
        val client: Socket = server.accept()
        println("Client connected: ${client.inetAddress.hostAddress}")

        Thread {
            val input =
                BufferedReader(InputStreamReader(client.getInputStream()))

            val command = input.readLine()
            println("Received command: $command")

            // Stream response to the client
            processCommand(burrow, command, client.getOutputStream())

            client.close()
        }.start()
    }
}

fun processCommand(
    burrow: Burrow,
    command: String,
    outputStream: OutputStream
) {
    burrow.parse(command, Environment(outputStream, "~", 80))
}
