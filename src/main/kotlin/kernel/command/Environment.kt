package burrow.kernel.command

import java.io.InputStream
import java.io.OutputStream

data class Environment(
    val inputStream: InputStream,
    val outputStream: OutputStream,
    val workingDirectory: String,
    val terminalSize: TerminalSize
)

data class TerminalSize(val width: Int, val height: Int) {
    override fun toString(): String = "$width $height"
}