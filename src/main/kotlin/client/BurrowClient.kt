package burrow.client

import burrow.carton.server.Endpoint
import burrow.carton.server.SocketService.Companion.SERVICE_LOCK_FILE
import burrow.kernel.Burrow
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.exists

fun configureLogging(
    logFilePath: String,
    logLevel: Level = Level.INFO,
    maxHistory: Int = 30,
    totalSizeCap: FileSize = FileSize.valueOf("1GB")
) {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    loggerContext.reset() // Reset any existing configuration

    // Create pattern encoder
    val encoder = PatternLayoutEncoder().apply {
        context = loggerContext
        pattern =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
        start()
    }

    // Create rolling file appender
    val rollingFileAppender = RollingFileAppender<ILoggingEvent>().apply {
        context = loggerContext
        name = "FILE"
        file = logFilePath

        // Configure rolling policy
        val rollingPolicy = TimeBasedRollingPolicy<ILoggingEvent>()
        rollingPolicy.context = loggerContext
        rollingPolicy.fileNamePattern = "${logFilePath}.%d{yyyy-MM-dd}.gz"
        rollingPolicy.maxHistory = maxHistory
        rollingPolicy.setTotalSizeCap(totalSizeCap)
        rollingPolicy.setParent(this)
        rollingPolicy.start()

        this.rollingPolicy = rollingPolicy
        this.encoder = encoder
        start()
    }

    // Configure root logger
    loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).apply {
        level = logLevel
        addAppender(rollingFileAppender)
    }
}

/**
 * Creates and returns an appropriate client implementation based on the
 * service state.
 *
 * This function determines whether to create a socket-based or local client by
 * checking the existence of the service lock file. The decision process works
 * as follows:
 *
 * 1. If the service lock file exists:
 *    - Reads the endpoint information from the first line of the lock file
 *    - Creates and returns a [SocketBasedClient] connected to that endpoint
 *
 * 2. If the service lock file does not exist:
 *    - Creates and returns a [LocalClient] for direct communication
 *
 * @return [Client] An instance of either [SocketBasedClient] or [LocalClient]
 *
 * @see burrow.carton.server.Server The service that clients connect to
 */
@Throws(IOException::class, IllegalStateException::class)
fun getClient(): Client {
    val burrowPath = Burrow.getBurrowRootPath()
    val serviceLockFilePath = burrowPath.resolve(SERVICE_LOCK_FILE)
    return when (serviceLockFilePath.exists()) {
        true -> {
            val firstLine = Files.readAllLines(serviceLockFilePath)[0]
            val endPoint = Endpoint.fromString(firstLine)
            SocketBasedClient(endPoint)
        }
        false -> {
            LocalClient()
        }
    }
}