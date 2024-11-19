package burrow.client

import burrow.kernel.buildBurrow
import burrow.kernel.command.Environment
import burrow.common.CommandLexer
import picocli.CommandLine
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

    override fun executeCommand(args: Array<String>): Int {
        try {
            val pipedOutputStream = PipedOutputStream()
            val pipedInputStream = PipedInputStream(pipedOutputStream)

            val environment = Environment(
                pipedOutputStream,
                System.getProperty("user.dir"),
                80
            )
            burrow.parse(args, environment)
            return processInputStreamForExitCode(pipedInputStream)
        } catch (ex: IOException) {
            System.err.println("IO exception encountered!")
            ex.printStackTrace()
            return CommandLine.ExitCode.SOFTWARE
        }
    }

    override fun close() {
        burrow.destroy()
    }
}