package burrow.client

import kotlin.system.exitProcess

class BurrowClient {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("slf4j.internal.verbosity", "WARN")
            val exitCode = LocalClient().executeCommand(args)
            exitProcess(exitCode)
        }
    }
}