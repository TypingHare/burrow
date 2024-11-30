package burrow.kernel.command

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ExtendedChamberModule
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.FurnishingNotFoundException
import burrow.kernel.furnishing.FurnishingProvider
import burrow.kernel.stream.StateOutputController
import picocli.CommandLine
import picocli.CommandLine.IExecutionExceptionHandler
import picocli.CommandLine.IParameterExceptionHandler
import java.util.concurrent.Callable
import kotlin.Throws
import kotlin.reflect.KClass

abstract class Command(val data: CommandData) :
    ExtendedChamberModule(data.chamber), Callable<Int>,
    IParameterExceptionHandler, IExecutionExceptionHandler, FurnishingProvider {
    companion object {
        fun extractCommandAnnotation(commandClass: CommandClass): CommandLine.Command =
            commandClass.java.getAnnotation(CommandLine.Command::class.java)
                ?: throw NotACommandException(commandClass.java.name)

        fun extractName(commandClass: CommandClass): String =
            extractCommandAnnotation(commandClass).name

        fun extractDescription(commandClass: CommandClass): String {
            val commandAnnotation = extractCommandAnnotation(commandClass)
            val descriptionArray = commandAnnotation.description
            return if (descriptionArray.isNotEmpty()) descriptionArray[0] else ""
        }
    }

    protected val name = data.commandName
    protected val environment = data.environment

    private val stateOutputController =
        StateOutputController(environment.outputStream)
    protected val stdout =
        stateOutputController.getWriterForState(WriterState.STDOUT)
    protected val stderr =
        stateOutputController.getWriterForState(WriterState.STDERR)
    protected val exitCodeWriter =
        stateOutputController.getWriterForState(WriterState.EXIT_CODE)

    override fun call(): Int {
        return CommandLine.ExitCode.OK
    }

    @Throws(FurnishingNotFoundException::class)
    override fun <F : Furnishing> use(furnishingClass: KClass<F>): F =
        chamber.renovator.getFurnishing(furnishingClass)

    protected fun dispatch(
        commandClass: CommandClass,
        args: List<Any>
    ): Int {
        val stringArgs = args.map { it.toString() }
        val commandName = extractName(commandClass)
        val commandData =
            CommandData(chamber, commandName, stringArgs, environment)
        val command = commandClass.java
            .getConstructor(CommandData::class.java)
            .newInstance(commandData)

        return processor.execute(command, stringArgs)
    }

    protected fun dispatch(commandClass: CommandClass) =
        dispatch(commandClass, listOf())

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
        var cause = ex;
        while (cause != null && cause.cause !== cause) {
            stderr.println(cause.message)
        }

        return CommandLine.ExitCode.SOFTWARE
    }

    object WriterState {
        const val STDOUT = "\$STDOUT$"
        const val STDERR = "\$STDERR$"
        const val EXIT_CODE = "\$EXIT_CODE$"
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