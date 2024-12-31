package burrow.kernel.stream

import org.junit.jupiter.api.Test
import java.io.StringWriter
import kotlin.test.assertEquals

class OutputStatePrintWriterTest {

    @Test
    fun `flush writes state when condition is met`() {
        val stringWriter = StringWriter()
        val writer = StatePrintWriter(stringWriter, "READY") { it == "READY" }
        writer.println()
        assertEquals("\$READY\n\n", stringWriter.toString())
    }

    @Test
    fun `flush does not write state when condition is not met`() {
        val stringWriter = StringWriter()
        val writer = StatePrintWriter(stringWriter, "IDLE") { it == "RUNNING" }
        writer.println()
        assertEquals("\n", stringWriter.toString())
    }

//    @Test
//    fun `flush handles edge case with dollar prefix`() {
//        val stringWriter = StringWriter()
//        val writer = StatePrintWriter(stringWriter, "\$test") { true }
//
//        writer.flush()
//        // Assuming this is the bug behavior for now
//        assertEquals("\$test", stringWriter.toString())
//    }
}