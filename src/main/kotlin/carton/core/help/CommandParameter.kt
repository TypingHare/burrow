package burrow.carton.core.help

data class CommandParameter(
    val indexString: String,
    val startIndex: Int,
    val label: String,
    val description: List<String>,
    val isOptional: Boolean,
    val defaultValue: String
)