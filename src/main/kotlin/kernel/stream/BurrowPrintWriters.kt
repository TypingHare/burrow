package burrow.kernel.stream

import java.io.OutputStream
import java.io.PrintWriter

object BurrowPrintWriters {
    @JvmStatic
    private fun printWriter(
        outputStream: OutputStream,
        prefix: String
    ): PrintWriter =
        PrintWriter(PrefixedOutputStream(outputStream, prefix), true)

    fun stdout(outputStream: OutputStream) =
        printWriter(outputStream, Prefix.STDOUT)

    fun stderr(outputStream: OutputStream) =
        printWriter(outputStream, Prefix.STDERR)

    fun exitCode(outputStream: OutputStream) =
        printWriter(outputStream, Prefix.EXIT_CODE)

    object Prefix {
        const val STDOUT = "STDOUT\n"
        const val STDERR = "STDERR\n"
        const val EXIT_CODE = "EXIT_CODE\n"
    }

    object Suffix {
        const val INVISIBLE_SYMBOL = "\\i"
        const val NEWLINE_CANCEL_SYMBOL = "\\c"
    }
}