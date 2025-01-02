package burrow.kernel

import burrow.kernel.chamber.BuildChamberException
import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.event.EventBus
import burrow.kernel.furniture.Warehouse
import burrow.kernel.path.PathBound
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.Environment
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isDirectory

class Burrow : PathBound {
    private val path = getBurrowRootPath()

    val courier = EventBus()
    val warehouse = Warehouse()
    val chamberShepherd = ChamberShepherd(this)

    override fun getPath(): Path = path

    @Throws(BuildBurrowException::class)
    fun build() {
        try {
            ensureRootDir()
            initializeWarehouse()
            chamberShepherd.buildChamber(ChamberShepherd.ROOT_CHAMBER_NAME)
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
        val chamberName =
            if (hasChamberName) args[0] else ChamberShepherd.ROOT_CHAMBER_NAME
        val primaryArgs =
            if (hasChamberName) args.subList(1, args.size) else args

        val chamber: Chamber
        try {
            chamber = chamberShepherd[chamberName.trim()]
        } catch (ex: Exception) {
            throw BuildChamberException(chamberName, ex)
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
                path.resolve(LIBS_DIR),
                javaClass.classLoader,
                Properties().apply { set("burrow.packages", "burrow.carton") }
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
         * The relative path to the Burrow root directory.
         */
        private const val ROOT_DIR = ".burrow"

        /**
         * The relative path to the libs root directory.
         */
        const val LIBS_DIR = "libs"

        /**
         * Retrieves Burrow root path.
         */
        fun getBurrowRootPath(): Path = Path.of(System.getProperty("user.home"))
            .resolve(ROOT_DIR)
            .normalize()
    }
}

fun createBurrow(
    logger: Logger = LoggerFactory.getLogger(Burrow::class.java)
): Burrow {
    try {
        return Burrow()
    } catch (ex: Throwable) {
        logger.error(ex.message, ex)
        throw ex
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