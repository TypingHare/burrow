package burrow.kernel

import burrow.common.event.EventBus
import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.furniture.Warehouse
import burrow.kernel.furniture.Warehouse.Companion.PROPERTY_PACKAGES
import burrow.kernel.path.PathBound
import burrow.kernel.stream.StateWriterController
import burrow.kernel.stream.state.OutputState
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Environment
import burrow.kernel.terminal.ExitCode
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isDirectory
import kotlin.time.measureTimedValue

class Burrow : PathBound {
    private val path = getBurrowRootPath()

    val courier = EventBus()
    val warehouse = Warehouse()
    val chamberShepherd = ChamberShepherd(this)

    override fun getPath(): Path = path

    @Throws(BuildBurrowException::class)
    fun build() {
        try {
            val durationMs = measureTimedValue {
                ensureRootDir()
                initializeWarehouse()
                chamberShepherd.buildChamber(ChamberShepherd.ROOT_CHAMBER_NAME)
            }.duration.inWholeMilliseconds

            logger.info("Built Burrow in $durationMs ms")
        } catch (ex: Exception) {
            throw BuildBurrowException(ex)
        }
    }

    fun destroy() {
        chamberShepherd.chambers.keys.toList().forEach {
            chamberShepherd.destroyChamber(it)
        }
    }

    fun parse(args: List<String>, environment: Environment) {
        val hasChamberName = args.isNotEmpty() && !args[0].startsWith("-")
        val hasRootChamberIndicator = args.isNotEmpty() && args[0].startsWith(
            ROOT_CHAMBER_INDICATOR
        )
        val chamberName =
            getChamberName(args, hasChamberName, hasRootChamberIndicator)
        val primaryArgs =
            getPrimaryArgs(args, hasChamberName, hasRootChamberIndicator)

        val chamber: Chamber
        try {
            chamber = chamberShepherd[chamberName.trim()]
        } catch (ex: Exception) {
            StateWriterController(environment.outputStream).apply {
                getPrintWriter(OutputState.STDERR).println(ex.message)
                getPrintWriter(OutputState.EXIT_CODE).println(ExitCode.SOFTWARE)
            }

            throw ex
        }

        val hasCommandName =
            primaryArgs.isNotEmpty() && !primaryArgs[0].startsWith("-")
        val commandName =
            if (hasCommandName) primaryArgs[0]
            else chamber.interpreter.defaultCommandName.get()
        val secondaryArgs = if (hasCommandName) primaryArgs.subList(
            1,
            primaryArgs.size
        ) else primaryArgs
        val commandData = CommandData(
            chamber,
            secondaryArgs,
            environment
        )
        chamber.interpreter.execute(commandName, commandData)
    }

    private fun getChamberName(
        args: List<String>,
        hasChamberName: Boolean,
        hasRootChamberIndicator: Boolean
    ): String {
        if (hasRootChamberIndicator) return ChamberShepherd.ROOT_CHAMBER_NAME
        if (hasChamberName) return args[0]

        return ChamberShepherd.ROOT_CHAMBER_NAME
    }

    private fun getPrimaryArgs(
        args: List<String>,
        hasChamberName: Boolean,
        hasRootChamberIndicator: Boolean
    ): List<String> {
        if (hasRootChamberIndicator) {
            return mutableListOf(args[0].substring(1)).apply {
                addAll(args.drop(1))
            }
        }
        if (hasChamberName) return args.drop(1)

        return args
    }

    @Throws(CreateRootDirectoryException::class)
    private fun ensureRootDir() {
        if (path.isDirectory()) {
            return
        }

        if (!path.toFile().mkdirs()) {
            throw CreateRootDirectoryException(path)
        }

        initializeFiles()
    }

    @Throws(CreateChambersDirectoryException::class)
    private fun initializeFiles() {
        val chamberDirPath = chamberShepherd.getPath()
        if (!chamberDirPath.toFile().mkdirs()) {
            throw CreateChambersDirectoryException(chamberDirPath)
        }
    }

    @Throws(InitializeWarehouseException::class)
    private fun initializeWarehouse() {
        try {
            warehouse.scanPackage(
                path.resolve(LIBS_DIR).resolve(JAR_NAME),
                javaClass.classLoader,
                Properties().apply { set(PROPERTY_PACKAGES, DEFAULT_PACKAGES) }
            )
        } catch (ex: Exception) {
            throw InitializeWarehouseException(ex)
        }
    }

    companion object {
        /**
         * The version of the Burrow kernel.
         */
        const val VERSION = "0.0.0"

        /**
         * The name of the fat jar file.
         */
        const val JAR_NAME = "burrow.jar"

        /**
         * The relative path to the Burrow root directory.
         */
        private const val ROOT_DIR = ".burrow"

        /**
         * Default package to scan.
         */
        const val DEFAULT_PACKAGES = "burrow.carton"

        /**
         * The relative path to the "libs" directory.
         */
        const val LIBS_DIR = "libs"

        /**
         * The relative path to the bin directory.
         */
        const val BIN_DIR = "bin"

        /**
         * The root chamber indicator can be used to specify the root chamber
         * directly.
         */
        const val ROOT_CHAMBER_INDICATOR = "@"

        /**
         * Retrieves Burrow root path.
         */
        fun getBurrowRootPath(): Path = Path.of(System.getProperty("user.home"))
            .resolve(ROOT_DIR)
            .normalize()

        private val logger = LoggerFactory.getLogger(Burrow::class.java)
    }
}

class BuildBurrowException(cause: Throwable) :
    RuntimeException("Failed to build the Burrow!", cause)

class CreateRootDirectoryException(rootDirPath: Path) :
    RuntimeException("Failed to create root directory: $rootDirPath")

class CreateChambersDirectoryException(chambersDirPath: Path) :
    RuntimeException("Failed to create chambers directory: $chambersDirPath")

class InitializeWarehouseException(cause: Throwable) :
    RuntimeException("Failed to initialize Warehouse!", cause)