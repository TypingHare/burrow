package burrow.carton.core.help

import burrow.kernel.terminal.CommandClass
import burrow.kernel.terminal.Option
import burrow.kernel.terminal.Parameters

object CommandParser {
    fun getCommandParameters(commandClass: CommandClass): List<CommandParameter> {
        val parameters = mutableListOf<CommandParameter>()
        for (field in commandClass.java.declaredFields) {
            val annotation =
                field.getAnnotation(Parameters::class.java) ?: continue
            val defaultValue = annotation.defaultValue
            parameters.add(
                CommandParameter(
                    annotation.index,
                    annotation.index.toInt(),
                    annotation.paramLabel.ifBlank { toKebabString(field.name) },
                    annotation.description.toList(),
                    defaultValue != "__no_default_value__",
                    defaultValue,
                )
            )
        }

        return parameters
    }

    fun getCommandOptions(commandClass: CommandClass): List<CommandOption> {
        val options = mutableListOf<CommandOption>()
        for (field in commandClass.java.declaredFields) {
            val annotation =
                field.getAnnotation(Option::class.java) ?: continue
            val names = annotation.names.toList()
            val longName = getLongName(names)
            val shortName = getShortName(names)
            val paramLabel = annotation.paramLabel.ifBlank {
                if (longName != null) return@ifBlank longName
                if (shortName != null) return@ifBlank shortName
                else return@ifBlank toKebabString(field.name)
            }
            options.add(
                CommandOption(
                    longName,
                    shortName,
                    paramLabel,
                    annotation.description.toList(),
                    if (annotation.defaultValue == "__no_default_value__") "" else annotation.defaultValue,
                )
            )
        }

        return options
    }

    private fun toKebabString(camelCaseString: String): String {
        return camelCaseString
            .replace(Regex("([a-z])([A-Z])"), "$1-$2")
            .lowercase()
    }

    private fun getLongName(names: List<String>): String? =
        names.firstOrNull { it.startsWith("--") }

    private fun getShortName(names: List<String>): String? =
        names.firstOrNull { it.startsWith("-") && it.length >= 2 && it[1] != '-' }
}