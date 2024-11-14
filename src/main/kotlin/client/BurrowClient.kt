package burrow.client

import burrow.kernel.stream.BurrowPrintWriter
import burrow.server.BurrowServer
import picocli.CommandLine
import java.io.*
import java.net.Socket

class BurrowClient(host: String, port: Int) :
    Closeable {
    private enum class ResponseType {
        NONE,
        STDOUT,
        STDERR,
        EXIT_CODE
    }

    companion object {
        private val responseTypeMap = mapOf(
            BurrowPrintWriter.Prefix.STDOUT.trim() to ResponseType.STDOUT,
            BurrowPrintWriter.Prefix.STDERR.trim() to ResponseType.STDERR,
            BurrowPrintWriter.Prefix.EXIT_CODE.trim() to ResponseType.EXIT_CODE
        )
    }

    private val clientSocket = Socket(host, port)
    private var currentResponseType = ResponseType.NONE
    private var stdoutOutputStream: OutputStream = System.out
    private var stderrOutputStream: OutputStream = System.err

    fun setStdoutOutputStream(stdoutOutputStream: OutputStream) {
        this.stdoutOutputStream = stdoutOutputStream
    }

    fun setStderrOutputStream(stderrOutputStream: OutputStream) {
        this.stderrOutputStream = stderrOutputStream
    }

    fun send(command: String): Int {
        try {
            val reader =
                BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val writer = PrintWriter(clientSocket.getOutputStream(), true)

            writer.println(command)

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val responseType = responseTypeMap[line]
                if (responseType != null) {
                    currentResponseType = responseType
                    continue
                }

                when (currentResponseType) {
                    ResponseType.STDOUT -> writeStdout(line)
                    ResponseType.STDERR -> writeStderr(line)
                    ResponseType.EXIT_CODE -> return line!!.toInt()
                    ResponseType.NONE -> continue
                }
            }
        } catch (ex: IOException) {
            throw RuntimeException("Failed to send command to the server.", ex)
        }

        return CommandLine.ExitCode.OK
    }

    override fun close() {
        clientSocket.close()
    }

    private fun writeStdout(line: String?) {
        if (line != null) {
            stdoutOutputStream.write(line.toByteArray())
            stdoutOutputStream.write("\n".toByteArray())
        }
    }

    private fun writeStderr(line: String?) {
        if (line != null) {
            stderrOutputStream.write(line.toByteArray())
            stderrOutputStream.write("\n".toByteArray())
        }
    }
}

fun main() {
    BurrowClient("localhost", BurrowServer.Standard.PORT).use { client ->
        client.send("default root")
        println()
        client.send(". furnishings --all")
    }
}