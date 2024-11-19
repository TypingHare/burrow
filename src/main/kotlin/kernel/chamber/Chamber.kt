package burrow.kernel.chamber

import burrow.kernel.Burrow
import burrow.kernel.command.Processor
import burrow.kernel.config.Config
import burrow.kernel.event.EventBus
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.FurnishingNotFoundException
import burrow.kernel.furnishing.Renovator
import burrow.kernel.palette.Palette
import burrow.kernel.palette.PicocliPalette
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.reflect.KClass

class Chamber(val burrow: Burrow, val name: String) {
    val rootPath: Path = burrow.chambersPath.resolve(name).normalize()
    val config = Config()
    val renovator = Renovator(this)
    val processor = Processor(this)
    val affairManager = EventBus()
    var palette: Palette = PicocliPalette()

    @Throws(BuildChamberException::class)
    fun build() {
        try {
            checkChamberRootDirectory()
            loadFurnishings()
            loadConfig()
            renovator.initializeFurnishings()
        } catch (throwable: Throwable) {
            throw BuildChamberException(name, throwable)
        }
    }

    private fun checkChamberRootDirectory() {
        if (name.isEmpty() || !rootPath.isDirectory()) {
            throw ChamberNotFoundException(name)
        }
    }

    private fun loadFurnishings() {
        val furnishingsFilePath = getFurnishingsFilePath()
        if (!furnishingsFilePath.exists()) {
            throw FurnishingsFileNotFoundException(furnishingsFilePath)
        }

        val furnishingIds = loadFurnishingIds(furnishingsFilePath)
        renovator.loadFurnishings(furnishingIds)
    }

    private fun loadConfig() {
        val configFilePath = getConfigFilePath()
        if (!configFilePath.exists()) {
            throw ConfigFileNotFoundException(configFilePath)
        }

        config.importRawEntries(loadConfigRawEntries(configFilePath))
        config.isModified.set(false)
    }

    private fun saveConfig() {
        val rawEntries = config.exportRawEntries()
        val content = Gson().toJson(rawEntries)
        Files.write(getConfigFilePath(), content.toByteArray())
    }

    fun destroy() {
        // Discards all the furnishings
        renovator.depTree.resolve { it.discard() }

        // Save config
        saveConfig()
    }

    fun <F : Furnishing> use(furnishingClass: KClass<F>): F {
        return renovator.getFurnishing(furnishingClass)
            ?: throw FurnishingNotFoundException(furnishingClass.java.name)
    }

    private fun getFurnishingsFilePath(): Path =
        rootPath.resolve(Burrow.Standard.FURNISHINGS_FILE_NAME)

    private fun getConfigFilePath(): Path =
        rootPath.resolve(Burrow.Standard.CONFIG_FILE_NAME)

    private fun loadFurnishingIds(furnishingsFilePath: Path): List<String> {
        val content = Files.readString(furnishingsFilePath)
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(content, type)
    }

    private fun loadConfigRawEntries(configFilePath: Path): Map<String, String> {
        val content = Files.readString(configFilePath)
        val type = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(content, type)
    }
}

class ChamberNotFoundException(private val name: String) :
    RuntimeException("Chamber not found: $name") {
    fun getName(): String = name
}

class FurnishingsFileNotFoundException(private val path: Path) :
    RuntimeException("Furnishings file not found: $path") {
    fun getPath(): Path = path
}

class ConfigFileNotFoundException(private val path: Path) :
    RuntimeException("Config file not found: $path") {
    fun getPath(): Path = path
}

class BuildChamberException(private val name: String, cause: Throwable) :
    RuntimeException("Failed to build chamber: $name", cause) {
    fun getName(): String = name
}