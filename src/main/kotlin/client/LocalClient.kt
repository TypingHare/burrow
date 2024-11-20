package burrow.client

import burrow.common.CommandLexer
import burrow.kernel.buildBurrow
import burrow.kernel.command.Environment
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream

class LocalClient : Client() {
    val burrow = buildBurrow()

    override fun executeCommand(command: String): Int {
        return executeCommand(
            CommandLexer.tokenizeCommandString(command).toTypedArray()
        )
    }

    @Throws(IOException::class)
    override fun executeCommand(args: Array<String>): Int {
        val pipedOutputStream = PipedOutputStream()
        val pipedInputStream = PipedInputStream(pipedOutputStream)

        val environment = Environment(
            pipedOutputStream,
            getWorkingDirectory(),
            getTerminalSize()
        )
        burrow.parse(args, environment)
        return processInputStreamForExitCode(pipedInputStream)
    }

    override fun close() {
        burrow.destroy()
    }
}