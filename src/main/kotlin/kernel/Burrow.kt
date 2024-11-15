package burrow.kernel

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.command.CommandData
import burrow.kernel.command.Environment
import burrow.kernel.command.Processor
import burrow.kernel.event.EventBus
import burrow.kernel.furnishing.FurnishingWareHouse
import burrow.kernel.stream.BurrowPrintWriters
import ch.qos.logback.classic.Level
import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.io.path.isDirectory

class Burrow {
    companion object {
        val CLASS_LOADER: ClassLoader = Companion::class.java.classLoader
        val logger: Logger = LoggerFactory.getLogger(Burrow::class.java)
    }

    val rootPath: Path
    val chambersPath: Path
    val chamberShepherd = ChamberShepherd(this)
    val furnishingWarehouse = FurnishingWareHouse()
    val affairManager = EventBus()

    init {
        rootPath = Path.of(
            System.getProperty("user.home")
        ).resolve(
            getEnvOrDefault(
                EnvKey.ROOT_DIR,
                Default.ROOT_DIR
            )
        ).normalize()
        chambersPath = rootPath.resolve(
            getEnvOrDefault(
                EnvKey.CHAMBERS_DIR,
                Default.CHAMBERS_DIR
            )
        ).normalize()

        val start = Instant.now()
        ensureRootDir()
        initializeFurnishingWarehouse()
        buildRootChamber()
        val duration = Duration.between(start, Instant.now())
        logger.info("Started Burrow in {} ms", duration.toMillis())
    }

    fun parse(args: Array<String>, environment: Environment) =
        parse(args.toList(), environment)

    private fun parse(args: List<String>, environment: Environment) {
        val hasChamberName = args.isNotEmpty() && !args[0].startsWith("-")
        val chamberName =
            if (hasChamberName) args[0] else Standard.ROOT_CHAMBER_NAME
        val primaryArgs =
            if (hasChamberName) args.subList(1, args.size) else args

        val chamber: Chamber
        try {
            chamber = chamberShepherd[chamberName.trim()]
            val hasCommandName =
                primaryArgs.isNotEmpty() && !primaryArgs[0].startsWith("-")
            val commandName =
                if (hasCommandName) primaryArgs[0]
                else Processor.DEFAULT_COMMAND_NAME
            val secondaryArgs = if (hasCommandName) primaryArgs.subList(
                1,
                primaryArgs.size
            ) else primaryArgs
            val commandData = CommandData(
                chamber,
                commandName,
                secondaryArgs,
                environment
            )
            chamber.processor.execute(commandData)
        } catch (ex: Exception) {
            logger.error("Failed to initialize chamber: $chamberName", ex)
            BurrowPrintWriters.stderr(environment.outputStream)
                .println("Failed to initialize chamber: $chamberName")
            BurrowPrintWriters.exitCode(environment.outputStream)
                .println(CommandLine.ExitCode.USAGE)
        }
    }

    fun destroy() {
        val chamberNames = chamberShepherd.chambers.keys.toList()
        for (chamberName in chamberNames) {
            chamberShepherd.destroyChamber(chamberName)
        }
    }

    @Throws(BurrowInitializationException::class)
    private fun ensureRootDir() {
        if (rootPath.isDirectory()) {
            return
        }

        if (!rootPath.toFile().mkdirs()) {
            throw BurrowInitializationException(
                "Failed to create burrow root directory: $rootPath"
            )
        }

        initializeFiles()
    }

    @Throws(BurrowInitializationException::class)
    private fun initializeFiles() {
        if (!chambersPath.toFile().mkdirs()) {
            throw BurrowInitializationException(
                "Failed to create burrow chambers directory: $chambersPath"
            )
        }
    }

    private fun initializeFurnishingWarehouse() {
        furnishingWarehouse.scanPackage(
            CLASS_LOADER,
            setOf(Standard.CARTON_PACKAGE_NAME)
        )
    }

    private fun buildRootChamber() {
        chamberShepherd.buildChamber(Standard.ROOT_CHAMBER_NAME)
    }

    fun getEnvOrDefault(key: String, defaultValue: String): String =
        System.getenv(key) ?: defaultValue

    object EnvKey {
        const val ROOT_DIR = "BURROW_ROOT_DIR"
        const val CHAMBERS_DIR = "BURROW_CHAMBERS_DIR"
    }

    object Default {
        const val ROOT_DIR = ".burrow"
        const val CHAMBERS_DIR = "chambers"
    }

    object Standard {
        const val ROOT_CHAMBER_NAME = "."
        const val FURNISHINGS_FILE_NAME = "furnishings.json"
        const val CONFIG_FILE_NAME = "config.json"
        const val CARTON_PACKAGE_NAME = "burrow.carton"
    }
}

fun buildBurrow(): Burrow {
    System.setProperty("slf4j.internal.verbosity", "WARN")
    val reflectionsLogger =
        LoggerFactory.getLogger(Reflections::class.java)
                as ch.qos.logback.classic.Logger
    reflectionsLogger.level = Level.OFF

    return Burrow()
}

class BurrowInitializationException(message: String, cause: Throwable?) :
    RuntimeException(message, cause) {
    constructor(message: String) : this(message, null)
}