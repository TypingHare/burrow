package burrow.kernel.command

import java.io.OutputStream

data class Environment(
    val outputStream: OutputStream,
    val workingDirectory: String,
    val terminalSize: TerminalSize
)

data class TerminalSize(val width: Int, val height: Int) {
    override fun toString(): String {
        return "$width $height"
    }
}