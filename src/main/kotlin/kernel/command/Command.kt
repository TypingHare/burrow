package burrow.kernel.command

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.FurnishingNotFoundException
import burrow.kernel.stream.BurrowPrintWriters
import picocli.CommandLine
import java.util.concurrent.Callable
import kotlin.reflect.KClass

abstract class Command(data: CommandData) : ChamberModule(data.chamber),
    Callable<Int> {
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

            return commandAnnotation.description[0]
        }
    }

    protected val name = data.commandName
    protected val args = data.commandArgs
    private val environment = data.environment

    protected val stdout = BurrowPrintWriters.stdout(environment.outputStream)
    protected val stderr = BurrowPrintWriters.stderr(environment.outputStream)

    protected fun exit(exitCode: Int) {
        BurrowPrintWriters.exitCode(environment.outputStream).println(exitCode)
    }

    override fun call(): Int {
        return CommandLine.ExitCode.OK
    }

    protected fun <F : Furnishing> use(furnishingClass: KClass<F>): F {
        return renovator.getFurnishing(furnishingClass)
            ?: throw FurnishingNotFoundException(furnishingClass.java.name)
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