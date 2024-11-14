package burrow.kernel.command

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import burrow.kernel.event.Event
import burrow.kernel.stream.BurrowPrintWriter
import picocli.CommandLine

class Processor(chamber: Chamber) : ChamberModule(chamber) {
    companion object {
        const val DEFAULT_COMMAND_NAME = ""
    }

    private val commandClasses = mutableMapOf<String, CommandClass>()

    init {
        registerCommand(DefaultCommand::class)
        burrow.affairManager.subscribe(CommandNotFoundEvent::class) {
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
            burrow.affairManager.post(CommandNotFoundEvent(commandData))
            return
        }

        val commandClass = commandClasses[commandName]!!
        val outputStream = commandData.environment.outputStream
        try {
            val constructor =
                commandClass.java.getConstructor(CommandData::class.java)
            val command = constructor.newInstance(commandData)
            val commandArgs = commandData.commandArgs
            val exitCode =
                CommandLine(command).execute(*commandArgs.toTypedArray())
            BurrowPrintWriter.exitCode(outputStream).println(exitCode)
        } catch (ex: Throwable) {
            BurrowPrintWriter.stdout(outputStream).println(ex.message)
        }
    }

    object EventHandler {
        fun commandNotFoundEventHandler(event: CommandNotFoundEvent) {
            val commandName = event.commandData.commandName
            val outputStream = event.commandData.environment.outputStream
            BurrowPrintWriter.stderr(outputStream)
                .println("Command not found: $commandName")
            BurrowPrintWriter.exitCode(outputStream)
                .println(CommandLine.ExitCode.USAGE)
        }
    }
}

class CommandNotFoundEvent(val commandData: CommandData) : Event()