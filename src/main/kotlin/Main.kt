package burrow

import burrow.kernel.createBurrow
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.Environment

fun main() {
    System.setProperty("slf4j.internal.verbosity", "WARN")
    val burrow = createBurrow().apply { build() }

    val environment = Environment(
        System.`in`,
        System.out,
        mutableMapOf(
            Command.SessionContextKey.TERMINAL_SIZE to "120:36",
            Command.SessionContextKey.WORKING_DIRECTORY to System.getProperty("user.home")
        ),
    )

    burrow.parse(listOf(".", "--help"), environment)
    burrow.parse(listOf(".", "command"), environment)
//    burrow.parse(listOf("playground", "furnishing", "--all"), environment)
}