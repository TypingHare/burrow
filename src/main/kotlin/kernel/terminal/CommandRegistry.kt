package burrow.kernel.terminal

/**
 * Represents a registry for managing command classes.
 */
interface CommandRegistry {
    /**
     * Registers a command class to the registry. Once registered, the command
     * becomes available for use within the object.
     *
     * @param commandClass The command class to be registered.
     */
    fun registerCommand(commandClass: CommandClass)

    /**
     * Unregisters a command class from the registry. Once unregistered, the
     * command is no longer available for use within the object.
     *
     * @param commandClass The command class to be unregistered.
     */
    fun unregisterCommand(commandClass: CommandClass)
}