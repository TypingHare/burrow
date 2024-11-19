package burrow.kernel.command

import java.io.OutputStream

data class Environment(
    val outputStream: OutputStream,
    val workingDirectory: String,
    val consoleWidth: Int
)