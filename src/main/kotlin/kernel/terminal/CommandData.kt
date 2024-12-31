package burrow.kernel.terminal

import burrow.kernel.chamber.Chamber
import burrow.kernel.terminal.Environment

data class CommandData(
    val chamber: Chamber,
    val args: List<String>,
    val environment: Environment
)