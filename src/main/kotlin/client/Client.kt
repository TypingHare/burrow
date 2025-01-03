package burrow.client

import burrow.kernel.stream.StateBufferReader
import burrow.kernel.stream.state.OutputState
import burrow.kernel.terminal.ExitCode
import burrow.kernel.terminal.TerminalSize
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicInteger

abstract class Client : Closeable {
    private val stdoutStream = System.out
    private val stderrStream = System.err

    abstract fun executeCommand(command: String): Int
    abstract fun executeCommand(args: Array<String>): Int

    protected fun processInputStreamForExitCode(
        inputStream: InputStream,
    ): Int {
        val exitCode = AtomicInteger(ExitCode.OK)
        val stateBufferReader =
            StateBufferReader(
                BufferedReader(InputStreamReader(inputStream)),
                ""
            )
        stateBufferReader.readUntilNull { line, state, stopSignal ->
            when (state) {
                OutputState.STDOUT -> writeStdout(line)
                OutputState.STDERR -> writeStderr(line)
                OutputState.EXIT_CODE -> {
                    exitCode.set(line.toInt())
                    stopSignal.set(true)
                }
            }
        }

        return exitCode.get()
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