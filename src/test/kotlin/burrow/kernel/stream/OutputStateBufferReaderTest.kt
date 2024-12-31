package burrow.kernel.stream

import org.junit.jupiter.api.Assertions.assertEquals
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertNull

class OutputStateBufferReaderTest {
    @Test
    fun `readLine skips state lines and updates state`() {
        val stringReader = StringReader(
            """
                ${'$'}READY
                This is a test
                ${'$'}RUNNING
                Another test
            """.trimIndent()
        )
        val reader = StateBufferReader(stringReader, "idle")

        reader.readLine().apply {
            assertEquals("READY", reader.getCurrentState())
            assertEquals("This is a test", this)
        }

        reader.readLine().apply {
            assertEquals("RUNNING", reader.getCurrentState())
            assertEquals("Another test", this)
        }
    }

    @Test
    fun `readLine handles empty state lines`() {
        val stringReader = StringReader("\n\$READY\n")
        val reader = StateBufferReader(stringReader, "IDLE")

        reader.readLine().apply {
            assertEquals("IDLE", reader.getCurrentState())
            assertEquals("", this)
        }

        reader.readLine().apply {
            assertEquals("READY", reader.getCurrentState())
            assertNull(this)
        }
    }
}