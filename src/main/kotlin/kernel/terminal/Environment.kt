package burrow.kernel.terminal

import burrow.kernel.stream.StateBufferReader
import java.io.InputStream
import java.io.OutputStream

data class Environment(
    val inputStream: InputStream,
    val outputStream: OutputStream,
    val sessionContext: Map<String, String>
) {
    var stateBufferReader: StateBufferReader? = null
}
