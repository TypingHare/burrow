package burrow.kernel.command

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.FurnishingNotFoundException
import picocli.CommandLine
import java.io.PrintWriter
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
    }

    protected val name = data.commandName
    protected val args = data.commandArgs
    private val environment = data.environment

    protected val stdout = PrintWriter(System.out, true)
    protected val stderr = PrintWriter(environment.outputStream, true)

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