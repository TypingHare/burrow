package burrow.kernel.command

import java.util.regex.Pattern

object CommandUtility {
    /**
     * Constructs the original command string from an array of arguments.
     * @param args an array of command arguments
     * @return the original command string
     */
    @JvmStatic
    fun getOriginalCommand(args: Array<String>): String {
        return args.joinToString(" ") { arg ->
            if (arg.contains(" ")) "\"$arg\"" else arg
        }
    }

    /**
     * Splits a command string into individual arguments, handling quoted
     * strings as single arguments.
     * @param input the command string
     * @return a list of arguments
     */
    @JvmStatic
    fun splitArguments(input: String): List<String> {
        val arguments = mutableListOf<String>()
        val matcher =
            Pattern.compile("(?<=\\s|^)(\"(?:\\\\\"|[^\"])*\"|\\S+)(?=\\s|$)")
                .matcher(input)

        while (matcher.find()) {
            var argument = matcher.group(1)
            // Remove surrounding quotes if present
            if (argument.startsWith("\"") && argument.endsWith("\"")) {
                argument = argument.substring(1, argument.length - 1)
            }
            // Replace escaped quotes with a plain quote
            argument = argument.replace("\\\"", "\"")
            arguments.add(argument)
        }

        return arguments
    }
}
