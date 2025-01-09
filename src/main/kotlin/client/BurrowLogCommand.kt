package burrow.client

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    System.setProperty("slf4j.internal.verbosity", "WARN")
    getClient()
        .use { it.executeCommand(args) }
        .let { exitProcess(it) }
}