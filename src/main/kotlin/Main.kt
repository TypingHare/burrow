package burrow

import burrow.kernel.buildBurrow
import burrow.kernel.command.Environment

fun main() {
    val burrow = buildBurrow()
    burrow.chamberShepherd["default"]

    val environment = Environment(System.out, "~", 80)
    burrow.parse(listOf(".", "--help"), environment)
    burrow.parse("default trump won", environment)

    burrow.destroy()
}
