package burrow.kernel.stream

import java.io.OutputStream

class StateOutputStream(
    private val controller: StateOutputController,
    private val outputStream: OutputStream,
    val state: String
) : OutputStream() {
    private val buffer = StringBuilder()

    override fun write(b: Int) {
        synchronized(buffer) {
            buffer.append(b.toChar())
        }
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        synchronized(buffer) {
            buffer.append(String(b, off, len))
        }
    }

    override fun flush() {
        synchronized(buffer) {
            controller.updateStateIfChanged(this)
            outputStream.write(buffer.toString().toByteArray())
            outputStream.flush()
            buffer.clear()
        }
    }

    override fun close() {
        flush()
        outputStream.close()
    }
}