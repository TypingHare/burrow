package burrow.kernel

import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.event.EventBus
import burrow.kernel.furnishing.Warehouse
import org.slf4j.Logger
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.io.path.isDirectory

class Burrow(private val logger: Logger) : DirectoryBound(getRootDirPath()) {
    private val chamberShepherd = ChamberShepherd(this)
    val warehouse = Warehouse()
    val courier = EventBus()

    @Throws(BuildBurrowException::class)
    fun build() {
        val start = Instant.now()

        try {
            ensureRootDir()
            initializeWarehouse()
        } catch (ex: Exception) {
            throw BuildBurrowException(ex)
        }

        val duration = Duration.between(start, Instant.now())
        logger.info("Started Burrow in {} ms", duration.toMillis())

        // Build the root chamber
        chamberShepherd.buildChamber(ChamberShepherd.ROOT_CHAMBER_NAME)
    }

    @Throws(CreateRootDirectoryException::class)
    private fun ensureRootDir() {
        if (rootDirPath.isDirectory()) {
            return
        }

        if (!rootDirPath.toFile().mkdirs()) {
            throw CreateRootDirectoryException(rootDirPath)
        }

        initializeFiles()
    }

    @Throws(CreateChambersDirectoryException::class)
    private fun initializeFiles() {
        val chamberDirPath = chamberShepherd.rootDirPath
        if (!chamberDirPath.toFile().mkdirs()) {
            throw CreateChambersDirectoryException(chamberDirPath)
        }
    }

    @Throws(InitializeWarehouseException::class)
    private fun initializeWarehouse() {
        try {
            warehouse.scanPackage(
                rootDirPath.resolve(LIBS_DIR),
                Companion::class.java.classLoader,
                setOf("burrow.carton")
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
         * Returns the path of the root directory.
         */
        fun getRootDirPath(): Path =
            Path.of(System.getProperty("user.home"))
                .resolve(ROOT_DIR)
                .normalize()
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