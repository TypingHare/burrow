package burrow.client

import burrow.kernel.createBurrow
import burrow.kernel.terminal.Environment
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream

class LocalClient : Client() {
    val burrow = createBurrow().apply { build() }

    @Throws(IOException::class)
    override fun executeCommand(command: String): Int = executeCommand(
        CommandLexer.tokenizeCommandString(command).toTypedArray()
    )

    @Throws(IOException::class)
    override fun executeCommand(args: Array<String>): Int {
        val pipedOutputStream = PipedOutputStream()
        val pipedInputStream = PipedInputStream(pipedOutputStream)

        val environment = Environment(
            pipedInputStream,
            pipedOutputStream,
            mutableMapOf()
        )

        burrow.parse(args.toList(), environment)

        return processInputStreamForExitCode(pipedInputStream)
    }

    override fun close() {
        burrow.destroy()
    }
}