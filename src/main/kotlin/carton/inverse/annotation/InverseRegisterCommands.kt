package burrow.carton.inverse.annotation

/**
 * Furnishings marked with this annotation will undergo the following processes:
 * 1. During the assembling stage, the "command" package within the furnishing
 * package is scanned, and all the command classes are registered.
 */
annotation class InverseRegisterCommands