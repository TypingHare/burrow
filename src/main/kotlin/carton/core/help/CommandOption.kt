package burrow.carton.core.help

data class CommandOption(
    val longName: String?,
    val shortName: String?,
    val label: String,
    val description: List<String>,
    val defaultValue: String,
)