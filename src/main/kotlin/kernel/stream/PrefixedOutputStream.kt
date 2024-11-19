package burrow.kernel.stream

import java.io.OutputStream

class PrefixedOutputStream(
    private val out: OutputStream,
    private val prefix: String
) : OutputStream() {
    private val buffer = StringBuilder()

    override fun write(b: Int) {
        buffer.append(b.toChar())
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        buffer.append(String(b, off, len))
    }

    override fun flush() {
        val output = "$prefix$buffer"
        out.write(output.toByteArray())
        out.flush()
        buffer.clear()
    }

    override fun close() {
        flush()
        out.close()
    }
}