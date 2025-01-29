package burrow.kernel.terminal

import burrow.kernel.chamber.ExtendedChamberModule
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.FurnishingProvider
import burrow.kernel.furniture.NotDependencyFurnishingException
import burrow.kernel.stream.StateWriterController
import burrow.kernel.stream.state.OutputState
import picocli.CommandLine
import picocli.CommandLine.*
import picocli.CommandLine.ExitCode
import java.nio.file.Path
import java.util.concurrent.Callable
import kotlin.reflect.KClass

abstract class Command(val data: CommandData) :
    ExtendedChamberModule(data.chamber),
    Callable<Int>,
    IParameterExceptionHandler,
    IExecutionExceptionHandler,
    FurnishingProvider {

    private val stateWriterController =
        StateWriterController(data.environment.outputStream)
    val stdout =
        stateWriterController.getPrintWriter(OutputState.STDOUT)
    val stderr =
        stateWriterController.getPrintWriter(OutputState.STDERR)

    private val context = data.environment.sessionContext

    override fun <F : Furnishing> use(furnishingClass: KClass<F>): F {
        return renovator.getFurnishing(furnishingClass)
            ?: throw NotDependencyFurnishingException(furnishingClass.java.name)
    }

    private fun getTerminalSize() =
        TerminalSize.fromString(getContextValue(SessionContextKey.TERMINAL_SIZE))

    fun getTerminalWidth(): Int = getTerminalSize().width

    fun getWorkingDirectory(): Path =
        Path.of(getContextValue(SessionContextKey.WORKING_DIRECTORY))

    @Throws(MissingRequiredContextEntry::class)
    fun getContextValue(contextKey: String): String {
        return context[contextKey]
            ?: throw MissingRequiredContextEntry(contextKey)
    }

    fun dispatch(commandClass: CommandClass, args: List<Any>): Int {
        val stringArgs = args.map { it.toString() }
        val commandData =
            CommandData(data.chamber, stringArgs, data.environment)
        val command = commandClass.java
            .getConstructor(CommandData::class.java)
            .newInstance(commandData)

        return interpreter.execute(command, commandData.args)
    }

    fun readNextLine(type: ReadLineType): String? {
        stateWriterController.getPrintWriter(OutputState.READ_LINE)
            .println(type.name)
        val stateBufferReader = data.environment.stateBufferReader!!

        return stateBufferReader.readLine()
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
        fullParseResult: ParseResult?
    ): Int {
        interpreter.printErrorMessageRecursively(stderr, ex)
        return ExitCode.SOFTWARE
    }

    object SessionContextKey {
        const val TERMINAL_SIZE = "BURROW_SESSION_CONTEXT_TERMINAL_SIZE"
        const val WORKING_DIRECTORY = "BURROW_SESSION_CONTEXT_WORKING_DIRECTORY"
    }
}

fun extractBurrowCommand(commandClass: CommandClass): BurrowCommand =
    commandClass.java.getAnnotation(BurrowCommand::class.java)
        ?: throw NotACommandException(commandClass.java.name)

fun extractCommandName(commandClass: CommandClass): String =
    extractBurrowCommand(commandClass).name

fun extractHeader(commandClass: CommandClass): String =
    extractBurrowCommand(commandClass).header.let {
        it.firstOrNull() ?: ""
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