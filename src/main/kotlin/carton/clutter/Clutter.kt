package burrow.carton.clutter

import burrow.carton.clutter.command.CartonListCommand
import burrow.kernel.Burrow
import burrow.kernel.furniture.*
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.path.PathBound
import org.slf4j.LoggerFactory
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*
import kotlin.time.measureTimedValue

@Furniture(
    version = Burrow.VERSION,
    description = "Loads cartons into Burrow when launched.",
    type = Furniture.Type.ROOT
)
class Clutter(renovator: Renovator) : Furnishing(renovator), PathBound {
    private val path = burrow.getPath().resolve(Burrow.LIBS_DIR)
    private val cartonMap = mutableMapOf<Path, Carton>()

    override fun getPath(): Path = path

    override fun assemble() {
        registerCommand(CartonListCommand::class)
    }

    override fun launch() {
        loadCartons()
    }

    /**
     * Strips the burrow root path from an absolute path that starts with
     * burrow root path.
     */
    fun stripBurrowPath(pathString: String): String =
        pathString.substring(burrow.getPath().toString().length + 1)

    /**
     * Loads cartons in the lib directory. The cartons that are loaded will not
     * be loaded again.
     */
    private fun loadCartons() {
        val cartonJarPaths = (path.toFile().listFiles() ?: emptyArray())
            .filter { it.isFile() }
            .filter { it.name.endsWith(".carton.jar") }
            .map { it.toPath() }

        for (cartonJarPath in cartonJarPaths) {
            if (cartonMap.containsKey(cartonJarPath)) {
                continue
            }

            val timedValue = measureTimedValue {
                loadCarton(cartonJarPath)
            }
            val durationMs = timedValue.duration.inWholeMilliseconds
            logger.info("Loaded carton [$cartonJarPath] in $durationMs ms")

            cartonMap[cartonJarPath] = timedValue.value
        }
    }

    /**
     * Loads a carton. A resource file (properties file) is required in a
     * carton. A URL class loader is created. The path of the jar, the created
     * class loader, and properties loaded from the "properties file" are passed
     * to `warehouse.scanPackage`.
     * @see PROPERTIES_FILE
     * @see Warehouse
     */
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

    companion object {
        const val PROPERTIES_FILE = "carton.properties"

        private val logger = LoggerFactory.getLogger(Burrow::class.java)
    }
}

class CartonPropertiesFileNotFoundException(jarPath: Path) :
    RuntimeException("Carton properties (${Clutter.PROPERTIES_FILE}) not found in carton: $jarPath")

class InvalidCartonNameException(cartonName: String) :
    RuntimeException("Invalid carton name: $cartonName")