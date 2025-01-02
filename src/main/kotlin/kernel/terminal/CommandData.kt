package burrow.kernel.terminal

import burrow.kernel.chamber.Chamber

data class CommandData(
    val chamber: Chamber,
    val args: List<String>,
    val environment: Environment
)