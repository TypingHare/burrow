package burrow.kernel.stream

import java.io.Closeable
import java.io.OutputStream
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicReference

class StateWriterController(
    private val outputStream: OutputStream,
    private val defaultState: String
) : Closeable {
    private val currentState = AtomicReference(defaultState)
    private val statePrintWriters = mutableMapOf<String, StatePrintWriter>()

    private fun acceptState(state: String): Boolean {
        if (state != currentState.get()) {
            currentState.set(state)
            return true
        }

        return false
    }

    fun getPrintWriter(state: String): StatePrintWriter {
        return statePrintWriters.getOrPut(state) {
            StatePrintWriter(PrintWriter(outputStream, true), state) {
                acceptState(it)
            }
        }
    }

    override fun close() {
        statePrintWriters.values.forEach { it.close() }
    }
}