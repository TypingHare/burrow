package burrow.kernel.chamber

import burrow.kernel.Burrow
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals

class ChamberTest {
    @Test
    fun `should return`() {
        val burrow = Burrow(LoggerFactory.getLogger(Burrow::class.java))
        val chamberShepherd = ChamberShepherd(burrow)
        val chamber =
            Chamber(chamberShepherd, ChamberShepherd.ROOT_CHAMBER_NAME)

        assertEquals(chamber.name, ChamberShepherd.ROOT_CHAMBER_NAME)
    }
}