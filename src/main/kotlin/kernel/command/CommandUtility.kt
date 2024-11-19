package burrow.kernel.command

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
}
