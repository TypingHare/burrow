package burrow.carton.clutter

import burrow.kernel.Burrow
import burrow.kernel.BuildBurrowException
import burrow.kernel.chamber.Chamber
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.InvalidFurnishingClassException
import burrow.kernel.furnishing.annotation.Furniture
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isDirectory

@Furniture(
    version = Burrow.VERSION.NAME,
    description = "Clutter allows users to load cartons (JAR files) into Burrow.",
    type = Furniture.Type.ROOT
)
class Clutter(chamber: Chamber) : Furnishing(chamber) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    private val cartonsPath: Path
    private val cartonMap = mutableMapOf<Path, Carton>()

    init {
        cartonsPath = burrow.rootPath.resolve(
            Burrow.getEnvOrDefault(EnvKey.CARTON_DIR, Default.CARTON_DIR)
        ).normalize()
        ensureCartonsDirectory()
    }

    override fun assemble() {
        val jarFiles = mutableListOf<File>()
        for (file in cartonsPath.toFile().listFiles()!!) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                jarFiles.add(file)
            }
        }

        for (jarFile in jarFiles) {
            try {
                val jarPath = jarFile.toPath()
                val carton = loadCarton(jarPath)
                cartonMap[jarPath] = carton
                logger.info("Loaded carton: {}", jarFile)
            } catch (ex: IOException) {
                logger.error("Failed to load carton: {}", jarFile, ex)
            } catch (ex: CartonPropertiesNotFoundException) {
                logger.error(ex.message, ex)
            } catch (ex: InvalidFurnishingClassException) {
                logger.error(
                    "Failed to load furnishings in carton: {}",
                    jarFile,
                    ex
                )
            }
        }
    }

    @Throws(CartonPropertiesNotFoundException::class)
    private fun loadCarton(jarPath: Path): Carton {
        val jarFileUri = URI.create("file://$jarPath")
        val jarUrl = URI.create("jar:$jarFileUri!/").toURL()
        val urlArray = arrayOf(jarUrl)

        val classLoader = URLClassLoader(urlArray, Burrow.CLASS_LOADER)
        val resourceUrl = classLoader.getResource(Standard.PROPERTIES_FILE)
            ?: throw CartonPropertiesNotFoundException(jarPath)

        val properties = Properties()
        resourceUrl.openStream().use { properties.load(it) }
        val packageListString =
            properties[JarPropertiesKey.PACKAGES] as? String
                ?: run {
                    logger.error(
                        "Mod JAR does not specify package list: {}",
                        jarPath.toAbsolutePath()
                    )
                    throw RuntimeException("Mod JAR does not specify package list")
                }

        // Scan the JAR file
        val packageList =
            packageListString.split(Standard.PACKAGE_SEPARATOR).toSet()
        val info = chamber.burrow.furnishingWarehouse.scanPackage(
            classLoader,
            packageList
        )

        return Carton(classLoader, properties, info)
    }

    private fun ensureCartonsDirectory() {
        if (cartonsPath.isDirectory()) {
            return
        }

        if (!cartonsPath.toFile().mkdirs()) {
            throw BuildBurrowException(
                "Failed to create cartons root directory: $cartonsPath"
            )
        }
    }

    object EnvKey {
        const val CARTON_DIR = "BURROW_CARTON_DIR"
    }

    object Default {
        const val CARTON_DIR = "cartons"
    }

    object Standard {
        const val PROPERTIES_FILE = "carton.properties"
        const val PACKAGE_SEPARATOR = ":"
    }

    object JarPropertiesKey {
        const val PACKAGES = "burrow.packages"
    }
}

class CartonPropertiesNotFoundException(jarPath: Path) :
    RuntimeException("Carton properties not found in carton: $jarPath")
