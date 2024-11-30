package burrow.kernel.command

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import burrow.kernel.event.Event
import burrow.kernel.stream.StateOutputController
import picocli.CommandLine
import java.util.concurrent.atomic.AtomicInteger

class Processor(chamber: Chamber) : ChamberModule(chamber) {
    private val commandClasses = mutableMapOf<String, CommandClass>()

    init {
        registerCommand(DefaultCommand::class)
        burrow.courier.subscribe(CommandNotFoundEvent::class) {
            EventHandler.commandNotFoundEventHandler(it)
        }
    }

    fun registerCommand(commandClass: CommandClass) {
        val name = Command.extractName(commandClass)
        commandClasses[name] = commandClass
    }

    fun execute(commandData: CommandData) {
        val commandName = commandData.commandName
        if (!commandClasses.containsKey(commandName)) {
            burrow.courier.post(CommandNotFoundEvent(commandData))
            return
        }

        val commandClass = commandClasses[commandName]!!
        val outputStream = commandData.environment.outputStream
        val writerManager = StateOutputController(outputStream)
        val exitCode = AtomicInteger(CommandLine.ExitCode.OK)
        try {
            val constructor =
                commandClass.java.getConstructor(CommandData::class.java)
            val command = constructor.newInstance(commandData)
            val commandArgs = commandData.commandArgs
            exitCode.set(execute(command, commandArgs))
        } catch (ex: Throwable) {
            exitCode.set(CommandLine.ExitCode.SOFTWARE)
            writerManager.getWriterForState(Command.WriterState.STDERR)
                .println(ex.message)
        } finally {
            writerManager.getWriterForState(Command.WriterState.EXIT_CODE)
                .println(exitCode.get())
        }
    }

    fun execute(command: Command, args: List<String>): Int =
        CommandLine(command)
            .setParameterExceptionHandler(command)
            .setExecutionExceptionHandler(command)
            .execute(*args.toTypedArray())

    companion object {
        const val DEFAULT_COMMAND_NAME = ""
    }

    object EventHandler {
        fun commandNotFoundEventHandler(event: CommandNotFoundEvent) {
            val commandName = event.commandData.commandName
            val outputStream = event.commandData.environment.outputStream
            val writerManager = StateOutputController(outputStream)
            writerManager.getWriterForState(Command.WriterState.STDERR)
                .println("Command not found: $commandName")
            writerManager.getWriterForState(Command.WriterState.EXIT_CODE)
                .println(CommandLine.ExitCode.USAGE)
        }
    }
}

class CommandNotFoundEvent(val commandData: CommandData) : Event()