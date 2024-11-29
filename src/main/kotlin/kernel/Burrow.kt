package burrow.kernel

import burrow.kernel.chamber.BuildChamberException
import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import burrow.kernel.command.Environment
import burrow.kernel.command.Processor
import burrow.kernel.event.EventBus
import burrow.kernel.furnishing.FurnishingWareHouse
import burrow.kernel.palette.Highlight
import burrow.kernel.palette.PicocliPalette
import burrow.kernel.stream.StreamWriterManager
import ch.qos.logback.classic.Level
import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.ExitCode
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.io.path.isDirectory

class Burrow {
    companion object {
        val CLASS_LOADER: ClassLoader = Companion::class.java.classLoader
        val logger: Logger = LoggerFactory.getLogger(Burrow::class.java)

        fun getRootPath(): Path {
            return Path.of(System.getProperty("user.home")).resolve(
                getEnvOrDefault(EnvKey.ROOT_DIR, Default.ROOT_DIR)
            ).normalize()
        }

        fun getEnvOrDefault(key: String, defaultValue: String): String =
            System.getenv(key) ?: defaultValue
    }

    val rootPath: Path = getRootPath()
    val chambersPath: Path
    val chamberShepherd = ChamberShepherd(this)
    val furnishingWarehouse = FurnishingWareHouse()
    val affairManager = EventBus()
    val palette = PicocliPalette()

    init {
        chambersPath = rootPath.resolve(
            getEnvOrDefault(EnvKey.CHAMBERS_DIR, Default.CHAMBERS_DIR)
        ).normalize()

        val start = Instant.now()
        ensureRootDir()
        initializeFurnishingWarehouse()
        buildRootChamber()
        val duration = Duration.between(start, Instant.now())
        val coloredBurrow = palette.color("Burrow", Highlights.BURROW)
        logger.info("Started $coloredBurrow in {} ms", duration.toMillis())
    }

    fun parse(args: Array<String>, environment: Environment) =
        parse(args.toList(), environment)

    @Throws(BuildBurrowException::class)
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
            throw BuildChamberException(chamberName, ex)
        }
    }

    fun destroy() {
        val chamberNames = chamberShepherd.chambers.keys.toList()
        for (chamberName in chamberNames) {
            chamberShepherd.destroyChamber(chamberName)
        }
    }

    @Throws(BuildBurrowException::class)
    private fun ensureRootDir() {
        if (rootPath.isDirectory()) {
            return
        }

        if (!rootPath.toFile().mkdirs()) {
            throw BuildBurrowException(
                "Failed to create burrow root directory: $rootPath"
            )
        }

        initializeFiles()
    }

    @Throws(BuildBurrowException::class)
    private fun initializeFiles() {
        if (!chambersPath.toFile().mkdirs()) {
            throw BuildBurrowException(
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

    object Highlights {
        val BURROW =
            Highlight(136, 0, Highlight.Style.ITALIC or Highlight.Style.BOLD)
        val CHAMBER = Highlight(169, 0, Highlight.Style.BOLD)
        val FURNISHING = Highlight(51, 0, Highlight.Style.ITALIC)
        val COMMAND = Highlight(214, 0, Highlight.Style.NONE)
    }

    object VERSION {
        const val NAME = "0.0.0"
    }
}

@Throws(BuildBurrowException::class)
fun buildBurrow(): Burrow {
    System.setProperty("slf4j.internal.verbosity", "WARN")
    val reflectionsLogger =
        LoggerFactory.getLogger(Reflections::class.java)
                as ch.qos.logback.classic.Logger
    reflectionsLogger.level = Level.OFF

    return Burrow()
}

class BuildBurrowException(message: String, cause: Throwable?) :
    RuntimeException(message, cause) {
    constructor(message: String) : this(message, null)
}