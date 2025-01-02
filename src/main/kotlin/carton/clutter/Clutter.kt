package burrow.carton.clutter

import burrow.kernel.Burrow
import burrow.kernel.furniture.*
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.path.PathBound
import java.io.File
import java.net.URI
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isDirectory

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
        val jarFiles = mutableListOf<File>()
        for (file in path.toFile().listFiles()!!) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                jarFiles.add(file)
            }
        }

        for (jarFile in jarFiles) {
            val jarPath = jarFile.toPath()
            val carton = loadCarton(jarPath)
            cartonMap[jarPath] = carton
        }
    }

    @Throws(CartonPropertiesNotFoundException::class)
    private fun loadCarton(jarPath: Path): Carton {
        val jarFileUri = URI.create("file://$jarPath")
        val jarUrl = URI.create("jar:$jarFileUri!/").toURL()
        val urlArray = arrayOf(jarUrl)

        val classLoader = URLClassLoader(urlArray, burrow.javaClass.classLoader)
        val resourceUrl = classLoader.getResource(PROPERTIES_FILE)
            ?: throw CartonPropertiesNotFoundException(jarPath)

        val properties = Properties()
        resourceUrl.openStream().use { properties.load(it) }

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
    }
}

class CartonPropertiesNotFoundException(jarPath: Path) :
    RuntimeException("Carton properties not found in carton: $jarPath")

class CreateCartonsDirectoryException(path: Path) :
    RuntimeException("Failed to create cartons directory: $path")