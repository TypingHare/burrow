package burrow.kernel.terminal

import burrow.kernel.chamber.ExtendedChamberModule
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.FurnishingProvider
import burrow.kernel.furniture.NotDependencyFurnishingException
import burrow.kernel.stream.StateWriterController
import picocli.CommandLine
import picocli.CommandLine.IParameterExceptionHandler
import picocli.CommandLine.IExecutionExceptionHandler
import picocli.CommandLine.ExitCode
import picocli.CommandLine.ParameterException
import picocli.CommandLine.MissingParameterException
import java.util.concurrent.Callable
import kotlin.jvm.Throws
import kotlin.reflect.KClass

abstract class Command(private val data: CommandData) :
    ExtendedChamberModule(data.chamber),
    Callable<Int>,
    IParameterExceptionHandler,
    IExecutionExceptionHandler,
    FurnishingProvider {

    private val stateWriterController =
        StateWriterController(data.environment.outputStream, OutputState.STDOUT)
    protected val stdout =
        stateWriterController.getPrintWriter(OutputState.STDOUT)
    protected val stderr =
        stateWriterController.getPrintWriter(OutputState.STDERR)

    private val context = data.environment.sessionContext
    protected val terminalSize =
        TerminalSize.parse(getContextValue(SessionContextKey.TERMINAL_SIZE))
    protected val workingDirectory =
        getContextValue(SessionContextKey.WORKING_DIRECTORY)

    override fun <F : Furnishing> use(furnishingClass: KClass<F>): F {
        return renovator.getFurnishing(furnishingClass)
            ?: throw NotDependencyFurnishingException(furnishingClass.java.name)
    }

    @Throws(MissingRequiredContextEntry::class)
    private fun getContextValue(contextKey: String): String {
        return context[contextKey]
            ?: throw MissingRequiredContextEntry(contextKey)
    }

    private fun dispatch(commandClass: CommandClass, args: List<Any>): Int {
        val stringArgs = args.map { it.toString() }
        val commandData =
            CommandData(data.chamber, stringArgs, data.environment)
        val command = commandClass.java
            .getConstructor(CommandData::class.java)
            .newInstance(commandData)

        return interpreter.execute(command, commandData.args)
    }

    protected fun dispatch(commandClass: CommandClass) =
        dispatch(commandClass, listOf())

    override fun handleParseException(
        ex: ParameterException,
        args: Array<String?>?
    ): Int {
        when (ex) {
            is MissingParameterException -> {
                val paramLabels =
                    ex.missing.joinToString(" ") { it.paramLabel() }
                stderr.println("Missing parameters: $paramLabels")
            }

            else -> {
                stderr.println("Failed to parse command: ${ex.message}")
            }
        }

        return ExitCode.SOFTWARE
    }

    override fun handleExecutionException(
        ex: Exception?,
        commandLine: CommandLine?,
        fullParseResult: CommandLine.ParseResult?
    ): Int {
        interpreter.printErrorMessageRecursively(stderr, ex)
        return CommandLine.ExitCode.SOFTWARE
    }

    object SessionContextKey {
        const val TERMINAL_SIZE = "BURROW_SESSION_CONTEXT_TERMINAL_SIZE"
        const val WORKING_DIRECTORY = "BURROW_SESSION_CONTEXT_WORKING_DIRECTORY"
    }
}

fun extractCommandAnnotation(commandClass: CommandClass): BurrowCommand =
    commandClass.java.getAnnotation(BurrowCommand::class.java)
        ?: throw NotACommandException(commandClass.java.name)

fun extractCommandName(commandClass: CommandClass): String {
    return extractCommandAnnotation(commandClass).name
}

class NotACommandException(className: String) :
    RuntimeException("Not a command class: $className")

class MissingRequiredContextEntry(contextKey: String) :
    RuntimeException("Missing required context entry: $contextKey")

typealias BurrowCommand = CommandLine.Command

typealias CommandClass = KClass<out Command>

typealias ExitCode = ExitCode
typealias Option = CommandLine.Option
typealias Parameters = CommandLine.Parameters