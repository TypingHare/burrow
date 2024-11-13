package burrow.kernel.chamber

import burrow.kernel.Burrow
import burrow.kernel.command.Processor
import burrow.kernel.config.Config
import burrow.kernel.furnishing.Renovator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class Chamber(val burrow: Burrow, val name: String) {
    private val rootPath: Path = burrow.chambersPath.resolve(name).normalize()
    val config = Config()
    val renovator = Renovator(this)
    val processor = Processor(this)

    fun build() {
        try {
            checkChamberRootDirectory()
            loadConfig()
        } catch (throwable: Throwable) {
            throw BuildChamberException(name, throwable)
        }
    }

    private fun checkChamberRootDirectory() {
        if (!rootPath.isDirectory()) {
            throw ChamberNotFoundException(name)
        }
    }

    private fun loadConfig() {
        val configFilePath = getConfigFilePath()
        if (!configFilePath.exists()) {
            throw ConfigFileNotFoundException(configFilePath)
        }

        val configRawEntries = loadConfigRawEntries(configFilePath)
        renovator.prepareConfig(config)
        val furnishingList =
            configRawEntries[Renovator.ConfigKey.FURNISHING_LIST]
                ?: Renovator.STANDARD_FURNISHING_ID
        config.importRawEntries(
            mapOf(Pair(Renovator.ConfigKey.FURNISHING_LIST, furnishingList))
        )
        renovator.loadFurnishings()
        config.importRawEntries(configRawEntries)
        renovator.initializeFurnishings()

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
        renovator.dependencyTree.resolve { it.discard() }

        // Save config
        saveConfig()
    }

    private fun getConfigFilePath(): Path =
        rootPath.resolve(Burrow.Standard.CONFIG_FILE_NAME)

    private fun loadConfigRawEntries(filePath: Path): Map<String, String> {
        val content = Files.readString(filePath)
        val type = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(content, type)
    }

    object ConfigKey {
        const val ALIAS = "alias"
        const val DESCRIPTION = "description"
    }
}

class ChamberNotFoundException(private val name: String) :
    RuntimeException("Chamber not found: $name") {
    fun getName(): String = name
}

class ConfigFileNotFoundException(private val path: Path) :
    RuntimeException("Config file not found: $path") {
    fun getPath(): Path = path
}

class BuildChamberException(private val name: String, cause: Throwable) :
    RuntimeException("Failed to build chamber: $name", cause) {
    fun getName(): String = name
}