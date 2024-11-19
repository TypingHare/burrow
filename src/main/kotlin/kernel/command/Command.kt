package burrow.kernel.command

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ExtendedChamberModule
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.stream.BurrowPrintWriters
import picocli.CommandLine
import picocli.CommandLine.IExecutionExceptionHandler
import picocli.CommandLine.IParameterExceptionHandler
import java.util.concurrent.Callable
import kotlin.reflect.KClass

abstract class Command(data: CommandData) : ExtendedChamberModule(data.chamber),
    Callable<Int>, IParameterExceptionHandler, IExecutionExceptionHandler {
    companion object {
        fun extractName(commandClass: CommandClass): String {
            val commandAnnotation =
                commandClass.java.getAnnotation(CommandLine.Command::class.java)
                    ?: throw NotACommandException(commandClass.java.name)

            return commandAnnotation.name
        }

        fun extractDescription(commandClass: CommandClass): String {
            val commandAnnotation =
                commandClass.java.getAnnotation(CommandLine.Command::class.java)
                    ?: throw NotACommandException(commandClass.java.name)
            val descriptionArray = commandAnnotation.description
            return if (descriptionArray.isNotEmpty()) descriptionArray[0] else ""
        }
    }

    protected val name = data.commandName
    protected val args = data.commandArgs
    protected val environment = data.environment

    protected val stdout = BurrowPrintWriters.stdout(environment.outputStream)
    protected val stderr = BurrowPrintWriters.stderr(environment.outputStream)

    protected fun exit(exitCode: Int) {
        BurrowPrintWriters.exitCode(environment.outputStream).println(exitCode)
    }

    override fun call(): Int {
        return CommandLine.ExitCode.OK
    }

    protected fun <F : Furnishing> use(furnishingClass: KClass<F>): F =
        chamber.use(furnishingClass)

    protected fun dispatch(
        commandClass: CommandClass,
        args: List<String>
    ): Int {
        val commandName = extractName(commandClass)
        val commandData = CommandData(chamber, commandName, args, environment)
        val command = commandClass.java
            .getConstructor(CommandData::class.java)
            .newInstance(commandData)
        return processor.execute(command, args)
    }

    /**
     * Handles exceptions thrown during command line parameter parsing.
     * @param ex   the exception thrown during parsing
     * @param args the arguments that were passed to the command
     * @return an exit code indicating the result of the error handling
     */
    override fun handleParseException(
        ex: CommandLine.ParameterException,
        args: Array<String?>?
    ): Int {
        when (ex) {
            is CommandLine.MissingParameterException -> {
                val paramLabels =
                    ex.missing.joinToString(" ") { it.paramLabel() }
                stderr.println("Missing parameters: $paramLabels")
            }

            else -> {
                stderr.println("Failed to parse command: ${ex.message}")
            }
        }
        return CommandLine.ExitCode.SOFTWARE
    }

    /**
     * Handles exceptions thrown during command execution.
     * @param ex              the exception thrown during execution
     * @param commandLine     the command line that was being executed
     * @param fullParseResult the result of parsing the command line arguments
     * @return an exit code indicating the result of the error handling
     */
    override fun handleExecutionException(
        ex: Exception?,
        commandLine: CommandLine?,
        fullParseResult: CommandLine.ParseResult?
    ): Int {
        ex?.let { stderr.println(it.message) }
        return CommandLine.ExitCode.SOFTWARE
    }
}

typealias CommandClass = KClass<out Command>

data class CommandData(
    val chamber: Chamber,
    val commandName: String,
    val commandArgs: List<String>,
    val environment: Environment
)

class NotACommandException(className: String) :
    RuntimeException("Not a command class: $className")