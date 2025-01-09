package burrow.client

import burrow.kernel.Burrow
import ch.qos.logback.classic.Level
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    System.setProperty("slf4j.internal.verbosity", "WARN")
    val logFile = Burrow.getBurrowRootPath()
        .resolve("logs/burrow_client.log")
    configureLogging(logFile.toString(), logLevel = Level.DEBUG)

    getClient()
        .use { it.executeCommand(args) }
        .let { exitProcess(it) }
}