package burrow.kernel.stream

import java.io.BufferedReader
import java.io.Reader
import java.util.concurrent.atomic.AtomicReference

class StateBufferReader(
    reader: Reader,
    defaultState: String
) : BufferedReader(reader) {
    private val currentState = AtomicReference(defaultState)

    override fun readLine(): String? {
        val nextLine: String? = super.readLine()
        if (nextLine != null && nextLine.startsWith("$")) {
            currentState.set(nextLine.substring(1))
            return readLine()
        }

        return nextLine
    }

    fun getCurrentState(): String = currentState.get()
}