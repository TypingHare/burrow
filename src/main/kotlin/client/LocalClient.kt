package burrow.client

import burrow.kernel.Burrow
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.Environment
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream

class LocalClient : Client() {
    val burrow = Burrow().apply { build() }

    @Throws(IOException::class)
    override fun executeCommand(command: String): Int = executeCommand(
        CommandLexer.tokenizeCommandString(command).toTypedArray()
    )

    @Throws(IOException::class)
    override fun executeCommand(args: Array<String>): Int {
        val pipedOutputStream = PipedOutputStream()
        val pipedInputStream = PipedInputStream(pipedOutputStream)

        val terminalSize = getTerminalSize()
        val environment = Environment(
            System.`in`,
            pipedOutputStream,
            mutableMapOf(
                Command.SessionContextKey.TERMINAL_SIZE to terminalSize.toString(),
                Command.SessionContextKey.WORKING_DIRECTORY to System.getProperty(
                    "user.home"
                )
            )
        )

        burrow.parse(args.toList(), environment)

        return processInputStreamForExitCode(pipedInputStream)
    }

    override fun close() {
        burrow.destroy()
    }
}