package burrow

import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.createBurrow
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Environment
import burrow.kernel.terminal.TerminalSize

fun main() {
    val burrow = createBurrow().apply { build() }
    val environment = Environment(
        System.`in`,
        System.out,
        mutableMapOf(
            Command.SessionContextKey.TERMINAL_SIZE to TerminalSize(
                120,
                36
            ).toString(),
            Command.SessionContextKey.WORKING_DIRECTORY to System.getProperty(
                "user.home"
            )
        )
    )

    burrow.parse(listOf(".", "furnishing"), environment)
}