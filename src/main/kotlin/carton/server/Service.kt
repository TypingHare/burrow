package burrow.carton.server

import burrow.kernel.Burrow
import org.slf4j.Logger
import java.io.Closeable
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.deleteIfExists

abstract class Service(
    protected val burrow: Burrow,
    protected val logger: Logger,
    protected val endpoint: Endpoint,
    private val serviceLockPath: Path,
) : Closeable {
    protected val isRunning = AtomicBoolean(false)

    init {
        writeServiceLockFile()
    }

    abstract fun listen()

    abstract fun receive(client: Socket)

    override fun close() {
        isRunning.set(false)
        serviceLockPath.deleteIfExists()
    }

    private fun writeServiceLockFile() {
        Files.write(serviceLockPath, endpoint.toString().toByteArray())
    }
}