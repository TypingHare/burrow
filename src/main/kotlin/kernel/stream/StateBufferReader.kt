package burrow.kernel.stream

import java.io.BufferedReader
import java.io.Reader
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class StateBufferReader(
    reader: Reader,
    defaultState: String
) : BufferedReader(reader) {
    private val currentState = AtomicReference(defaultState)

    override fun readLine(): String? {
        val nextLine: String? = super.readLine()
        if (nextLine != null && nextLine.trim().startsWith("$")) {
            currentState.set(nextLine.substring(1))
            return readLine()
        }

        return nextLine
    }

    fun getCurrentState(): String = currentState.get()

    fun readUntilNull(callback: (String, String, AtomicBoolean) -> Unit) {
        var line = ""
        val stopSignal = AtomicBoolean(false)
        while (readLine()?.also { line = it } != null) {
            callback(line, currentState.get(), stopSignal)
            if (stopSignal.get()) {
                break
            }
        }
    }
}