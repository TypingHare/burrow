package burrow.kernel.stream

import java.io.PrintWriter
import java.io.Writer

typealias StateCondition = (currentState: String) -> Boolean

class StatePrintWriter(
    private val writer: Writer,
    private val state: String,
    private val shouldPrintState: StateCondition
) : PrintWriter(writer, true) {
    private fun writeStateIfNeeded() {
        if (shouldPrintState(state)) {
            writer.write("\$$state\n")
            writer.flush()
        }
    }

    private fun <T> printWithState(value: T) {
        writeStateIfNeeded()
        super.println(escapeString(value.toString()))
    }

    private fun escapeString(string: String): String {
        // TODO: Implement escaping logic
        return string
    }

    override fun println(any: Any?) = printWithState(any)
    override fun println(x: String?) = printWithState(x)
    override fun println(x: Int) = printWithState(x)
    override fun println(x: Long) = printWithState(x)
    override fun println(x: Float) = printWithState(x)
    override fun println(x: Double) = printWithState(x)
    override fun println(x: Char) = printWithState(x)
    override fun println(x: CharArray) = printWithState(String(x))
    override fun println(x: Boolean) = printWithState(x)
    override fun println() {
        writeStateIfNeeded()
        super.println()
    }
}