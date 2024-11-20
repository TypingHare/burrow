package burrow.client

import burrow.kernel.command.TerminalSize
import burrow.kernel.stream.BurrowPrintWriters
import picocli.CommandLine.ExitCode
import java.io.*
import java.util.concurrent.atomic.AtomicReference


abstract class Client : Closeable {
    private val currentResponseType = AtomicReference(ResponseType.NONE)
    private var stdoutOutputStream: OutputStream = System.out
    private var stderrOutputStream: OutputStream = System.err

    fun setStdoutOutputStream(stdoutOutputStream: OutputStream) {
        this.stdoutOutputStream = stdoutOutputStream
    }

    fun setStderrOutputStream(stderrOutputStream: OutputStream) {
        this.stderrOutputStream = stderrOutputStream
    }

    abstract fun executeCommand(command: String): Int

    abstract fun executeCommand(args: Array<String>): Int

    protected fun processInputStreamForExitCode(
        inputStream: InputStream,
    ): Int {
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String
        while (reader.readLine().also { line = it } != null) {
            val exitCode = parseResponseLine(line)
            if (exitCode != null) {
                return exitCode
            }
        }

        return ExitCode.SOFTWARE
    }

    private fun parseResponseLine(line: String): Int? {
        val responseType = responseTypeMap[line]
        if (responseType != null) {
            currentResponseType.set(responseType)
            return null
        }

        when (currentResponseType.get()!!) {
            ResponseType.STDOUT -> writeStdout(line)
            ResponseType.STDERR -> writeStderr(line)
            ResponseType.EXIT_CODE -> return line.toInt()
            ResponseType.NONE -> return null
        }

        return null
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

    enum class ResponseType {
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
        fun getOriginalCommand(args: Array<String>): String {
            return args.joinToString(" ") { arg ->
                if (arg.contains(" ")) "\"$arg\"" else arg
            }
        }

        @JvmStatic
        fun getWorkingDirectory(): String {
            return System.getProperty("user.dir")
        }

        @JvmStatic
        fun getTerminalSize(): TerminalSize {
            return try {
                val process = ProcessBuilder("sh", "-c", "stty size < /dev/tty")
                    .redirectErrorStream(true)
                    .start()
                val reader = process.inputStream.bufferedReader()
                val (rows, cols) = reader.readLine().split(" ")
                    .map { it.toInt() }
                TerminalSize(cols, rows)
            } catch (ignore: Exception) {
                TerminalSize(80, 24)
            }
        }
    }
}