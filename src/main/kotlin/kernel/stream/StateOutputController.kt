package burrow.kernel.stream

import java.io.OutputStream
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicReference

class StateOutputController(private val outputStream: OutputStream) {
    private val currentState = AtomicReference("")
    private val writer = PrintWriter(outputStream, true)
    private val stateWriters = mutableMapOf<String, PrintWriter>()

    fun updateStateIfChanged(stateOutputStream: StateOutputStream) {
        val state = stateOutputStream.state
        if (currentState.getAndSet(state) != state) {
            synchronized(writer) {  // Ensure thread safety
                writer.println(state)
            }
        }
    }

    fun getWriterForState(state: String): PrintWriter =
        synchronized(stateWriters) {
            stateWriters.getOrPut(state) {
                PrintWriter(StateOutputStream(this, outputStream, state))
            }
        }

    fun escapeString(input: String): String {
        if (stateWriters.containsKey(input)) {
            return "$ESCAPE_CHAR$input"
        }

        return input.fold("") { acc, char ->
            if (char == ESCAPE_CHAR) "$acc$ESCAPE_CHAR$char" else "$acc$char"
        }
    }

    companion object {
        const val ESCAPE_CHAR = '\\'
    }

    fun closeAllWriters() {
        synchronized(stateWriters) {
            stateWriters.values.forEach { it.close() }
            stateWriters.clear()
        }
    }
}