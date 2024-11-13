package burrow.kernel.command

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import burrow.kernel.event.Event
import picocli.CommandLine
import java.io.PrintWriter

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
        try {
            val constructor =
                commandClass.java.getConstructor(CommandData::class.java)
            val command = constructor.newInstance(commandData)
            val commandArgs = commandData.commandArgs
            CommandLine(command).execute(*commandArgs.toTypedArray())
        } catch (ex: Throwable) {
            val stdout = PrintWriter(commandData.environment.outputStream)
            stdout.println(ex.message)
        }
    }

    object EventHandler {
        fun commandNotFoundEventHandler(event: CommandNotFoundEvent) {
            val commandName = event.commandData.commandName
            val stderr =
                PrintWriter(event.commandData.environment.outputStream, true)
            stderr.println("Command not found: $commandName")
        }
    }
}

class CommandNotFoundEvent(val commandData: CommandData) : Event()