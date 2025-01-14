package burrow.carton.clutter

import burrow.carton.clutter.command.CartonCommand
import burrow.kernel.Burrow
import burrow.kernel.furniture.*
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.path.PathBound
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isDirectory
import kotlin.time.measureTimedValue

@Furniture(
    version = Burrow.VERSION,
    description = "Allows users to load cartons (JAR files) into Burrow.",
    type = Furniture.Type.ROOT
)
class Clutter(renovator: Renovator) : Furnishing(renovator), PathBound {
    private val path = burrow.getPath().resolve(CARTON_DIR)
    private val cartonMap = mutableMapOf<Path, Carton>()

    init {
        ensureCartonsDirectory()
    }

    override fun getPath(): Path = path

    override fun assemble() {
        registerCommand(CartonCommand::class)
    }

    override fun launch() {
        val jarFiles = mutableListOf<File>()
        for (file in path.toFile().listFiles()!!) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                jarFiles.add(file)
            }
        }

        for (jarFile in jarFiles) {
            val jarPath = jarFile.toPath()
            val timedValue = measureTimedValue {
                loadCarton(jarPath)
            }
            val durationMs = timedValue.duration.inWholeMilliseconds
            logger.info("Loaded carton [$jarPath] in $durationMs ms")

            cartonMap[jarPath] = timedValue.value
        }
    }

    @Throws(CartonPropertiesFileNotFoundException::class)
    private fun loadCarton(jarPath: Path): Carton {
        val urlArray = arrayOf(jarPath.toUri().toURL())
        val classLoader = URLClassLoader(urlArray, burrow.javaClass.classLoader)
        val resourceUrl = classLoader.getResource(PROPERTIES_FILE)
            ?: throw CartonPropertiesFileNotFoundException(jarPath)

        val properties = Properties().apply {
            resourceUrl.openStream().use { load(it) }
        }

        return warehouse.scanPackage(jarPath, classLoader, properties)
    }

    private fun ensureCartonsDirectory() {
        if (path.isDirectory()) {
            return
        }

        if (!path.toFile().mkdirs()) {
            throw CreateCartonsDirectoryException(path)
        }
    }

    companion object {
        const val CARTON_DIR = "cartons"
        const val PROPERTIES_FILE = "carton.properties"

        private val logger = LoggerFactory.getLogger(Burrow::class.java)
    }
}

class CartonPropertiesFileNotFoundException(jarPath: Path) :
    RuntimeException("Carton properties (${Clutter.PROPERTIES_FILE}) not found in carton: $jarPath")

class CreateCartonsDirectoryException(path: Path) :
    RuntimeException("Failed to create cartons directory: $path")
