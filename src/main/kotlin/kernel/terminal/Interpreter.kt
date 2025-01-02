package burrow.kernel.terminal

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ExtendedChamberModule
import burrow.kernel.event.Event
import burrow.kernel.stream.StateWriterController
import burrow.kernel.stream.state.OutputState
import picocli.CommandLine
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class Interpreter(chamber: Chamber) : ExtendedChamberModule(chamber),
    CommandRegistry {
    private val commandClasses = mutableMapOf<String, CommandClass>()
    val defaultCommandName = AtomicReference(DEFAULT_COMMAND_NAME)

    override fun registerCommand(commandClass: CommandClass) {
        commandClasses[extractCommandName(commandClass)] = commandClass
    }

    override fun unregisterCommand(commandClass: CommandClass) {
        commandClasses.remove(extractCommandName(commandClass))
    }

    fun execute(command: Command, args: List<String>): Int =
        CommandLine(command)
            .setParameterExceptionHandler(command)
            .setExecutionExceptionHandler(command)
            .execute(*args.toTypedArray())

    fun execute(commandName: String, commandData: CommandData) {
        if (!commandClasses.containsKey(commandName)) {
            courier.post(CommandNotFoundEvent(commandName, commandData))
            return
        }

        val commandClass = commandClasses[commandName]!!
        val outputStream = commandData.environment.outputStream
        val stateWriterController =
            StateWriterController(outputStream)
        val exitCode = AtomicInteger(CommandLine.ExitCode.OK)
        try {
            val constructor =
                commandClass.java.getConstructor(CommandData::class.java)
            val command = constructor.newInstance(commandData)
            val commandArgs = commandData.args
            exitCode.set(execute(command, commandArgs))
        } catch (ex: Exception) {
            exitCode.set(CommandLine.ExitCode.SOFTWARE)
            printErrorMessageRecursively(
                stateWriterController.getPrintWriter(OutputState.STDERR), ex
            )
        } finally {
            stateWriterController
                .getPrintWriter(OutputState.EXIT_CODE)
                .println(exitCode.get())
        }
    }

    fun printErrorMessageRecursively(
        printWriter: PrintWriter,
        ex: Throwable?,
    ) {
        var currentEx: Throwable? = ex
        while (currentEx != null && currentEx.cause !== ex) {
            printWriter.println(
                """
                    [${currentEx.javaClass.simpleName}]  ${currentEx.message}
                """.trimIndent()
            )
            currentEx = currentEx.cause
        }
    }

    companion object {
        const val DEFAULT_COMMAND_NAME = "default"
    }
}

class CommandNotFoundEvent(
    val commandName: String,
    val commandData: CommandData
) : Event()