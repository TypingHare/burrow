package burrow.kernel.stream

import java.io.OutputStream
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicReference

class StreamWriterManager(private val outputStream: OutputStream) {
    companion object {
        const val ESCAPE_CHAR = '\\'
    }

    private val currentState = AtomicReference("")
    private val writer = PrintWriter(outputStream, true)
    private val stateWriters = mutableMapOf<String, StateAwareWriter>()

    fun updateStateIfChanged(stateWriter: StateAwareWriter) {
        if (currentState.get() != stateWriter.state) {
            currentState.set(stateWriter.state)
            writer.println(stateWriter.state)
        }
    }

    fun escapeString(input: String): String {
        if (stateWriters.containsKey(input)) {
            return "$ESCAPE_CHAR$input"
        }

        if (input.startsWith(ESCAPE_CHAR)) {
            val escapeCount = input.takeWhile { it == ESCAPE_CHAR }.length
            return ESCAPE_CHAR.toString().repeat(escapeCount) + input
        }

        return input
    }

    fun getWriterForState(state: String): StateAwareWriter =
        stateWriters.getOrPut(state) {
            StateAwareWriter(this, state, outputStream)
        }
}
