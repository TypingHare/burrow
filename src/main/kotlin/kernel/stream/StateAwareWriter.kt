package burrow.kernel.stream

import java.io.OutputStream
import java.io.PrintWriter

class StateAwareWriter(
    private val manager: StreamWriterManager,
    val state: String,
    outputStream: OutputStream
) : PrintWriter(outputStream, true) {
    override fun println(any: Any?) {
        manager.updateStateIfChanged(this)
        super.println(manager.escapeString(any.toString()))
    }

    override fun println(x: Int) {
        manager.updateStateIfChanged(this)
        super.println(manager.escapeString(x.toString()))
    }

    override fun println(x: Long) {
        manager.updateStateIfChanged(this)
        super.println(manager.escapeString(x.toString()))
    }

    override fun println(x: Float) {
        manager.updateStateIfChanged(this)
        super.println(manager.escapeString(x.toString()))
    }

    override fun println(x: Double) {
        manager.updateStateIfChanged(this)
        super.println(manager.escapeString(x.toString()))
    }

    override fun println(x: Char) {
        manager.updateStateIfChanged(this)
        super.println(manager.escapeString(x.toString()))
    }

    override fun println(x: CharArray) {
        manager.updateStateIfChanged(this)
        super.println(manager.escapeString(String(x)))
    }

    override fun println(x: Boolean) {
        manager.updateStateIfChanged(this)
        super.println(manager.escapeString(x.toString()))
    }

    override fun println() {
        manager.updateStateIfChanged(this)
        super.println()
    }
}
