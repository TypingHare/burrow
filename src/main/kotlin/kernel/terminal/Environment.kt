package burrow.kernel.terminal

import java.io.InputStream
import java.io.OutputStream

data class Environment(
    val inputStream: InputStream,
    val outputStream: OutputStream,
    val sessionContext: Map<String, String>
)
