package burrow.client

import kotlin.system.exitProcess

class BurrowClient {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode = LocalClient().executeCommand(args)
            exitProcess(exitCode)
        }
    }
}