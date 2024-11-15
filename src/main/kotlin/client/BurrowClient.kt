package burrow.client

import burrow.kernel.stream.BurrowPrintWriters
import burrow.server.BurrowServer
import picocli.CommandLine
import java.io.*
import java.net.Socket
import kotlin.system.exitProcess

class BurrowClient(host: String, port: Int) : Closeable {
    private val clientSocket: Socket
    private var currentResponseType = ResponseType.NONE
    private var stdoutOutputStream: OutputStream = System.out
    private var stderrOutputStream: OutputStream = System.err

    init {
        try {
            clientSocket = Socket(host, port)
        } catch (ex: IOException) {
            System.err.println("Failed to connect to Burrow server ($host:$port)!")
            exitProcess(CommandLine.ExitCode.SOFTWARE)
        }
    }

    fun setStdoutOutputStream(stdoutOutputStream: OutputStream) {
        this.stdoutOutputStream = stdoutOutputStream
    }

    fun setStderrOutputStream(stderrOutputStream: OutputStream) {
        this.stderrOutputStream = stderrOutputStream
    }

    fun send(args: Array<String>): Int {
        val commandString = getOriginalCommand(args)

        try {
            val reader =
                BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val writer = PrintWriter(clientSocket.getOutputStream(), true)

            writer.println(commandString)

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
            System.err.println("IO exception encountered!")
            ex.printStackTrace()
            return CommandLine.ExitCode.SOFTWARE
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

    private enum class ResponseType {
        NONE,
        STDOUT,
        STDERR,
        EXIT_CODE
    }

    companion object {
        private val responseTypeMap = mapOf(
            BurrowPrintWriters.Prefix.STDOUT.trim() to ResponseType.STDOUT,
            BurrowPrintWriters.Prefix.STDERR.trim() to ResponseType.STDERR,
            BurrowPrintWriters.Prefix.EXIT_CODE.trim() to ResponseType.EXIT_CODE
        )

        @JvmStatic
        fun main(args: Array<String>) {
            BurrowClient(
                "localhost",
                BurrowServer.Standard.PORT
            ).use { client ->
                val exitCode = client.send(args)
                exitProcess(exitCode)
            }
        }

        /**
         * Constructs the original command string from an array of arguments.
         * @param args an array of command arguments
         * @return the original command string
         */
        @JvmStatic
        fun getOriginalCommand(args: Array<String>): String {
            return args.joinToString(" ") { arg ->
                if (arg.contains(" ")) "\"$arg\"" else arg
            }
        }
    }
}