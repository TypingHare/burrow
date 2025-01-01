package burrow.client

import burrow.kernel.terminal.ExitCode
import burrow.kernel.stream.state.OutputState
import burrow.kernel.terminal.TerminalSize
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicReference

abstract class Client : Closeable {
    private val currentOutputState = AtomicReference(OutputState.STDOUT)
    private val stdoutStream = System.out
    private val stderrStream = System.err

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

        return ExitCode.OK
    }

    private fun parseResponseLine(line: String): Int? {
        if (line.startsWith("$")) {
            currentOutputState.set(line.substring(1))
            return null
        }

        return when (currentOutputState.get()!!) {
            OutputState.STDOUT -> writeStdout(line).let { null }
            OutputState.STDERR -> writeStderr(line).let { null }
            OutputState.EXIT_CODE -> return line.toInt()
            else -> null
        }
    }

    private fun writeStdout(line: String?) {
        if (line != null) {
            stdoutStream.write(line.toByteArray())
            stdoutStream.write("\n".toByteArray())
        }
    }

    private fun writeStderr(line: String?) {
        if (line != null) {
            stderrStream.write(line.toByteArray())
            stderrStream.write("\n".toByteArray())
        }
    }
}

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