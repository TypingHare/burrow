package burrow.client

import burrow.carton.server.Endpoint
import burrow.carton.server.SocketService.Companion.SERVICE_LOCK_FILE
import burrow.kernel.Burrow
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.system.exitProcess

class BurrowClient {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("slf4j.internal.verbosity", "WARN")

            val burrowPath = Burrow.getBurrowRootPath()
            val serviceLockFilePath = burrowPath.resolve(SERVICE_LOCK_FILE)
            val client = when (serviceLockFilePath.exists()) {
                true -> {
                    val firstLine = Files.readAllLines(serviceLockFilePath)[0]
                    val endPoint = Endpoint.fromString(firstLine)
                    SocketBasedClient(endPoint)
                }
                false -> {
                    LocalClient()
                }
            }

            val exitCode = client.use { it.executeCommand(args) }
            exitProcess(exitCode)
        }
    }
}