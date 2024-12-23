package burrow.kernel.config

import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.exists

class Config(chamber: Chamber) : ChamberModule(chamber) {
    private val configFilePath: Path =
        chamber.rootPath.resolve(Burrow.Standard.CONFIG_FILE_NAME)

    val itemHandlers = mutableMapOf<String, ConfigItemHandler<*>>()
    val entries = mutableMapOf<String, Any?>()
    val isModified = AtomicBoolean(false)

    operator fun <T> get(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return entries[key] as T?
    }

    operator fun set(key: String, value: Any?) {
        entries[key] = value
        isModified.set(true)
    }

    fun <T> getNotNull(key: String): T {
        return get<T>(key)!!
    }

    fun setIfAbsent(key: String, value: Any) {
        if (key !in entries || entries[key] == null) set(key, value)
    }

    fun addKey(key: String, handler: ConfigItemHandler<*>) {
        itemHandlers[key] = handler
    }

    fun <T> addKey(
        key: String,
        reader: ConfigItemReader<T>,
        writer: ConfigItemWriter<T>
    ) = addKey(key, ConfigItemHandler(reader, writer))

    fun addKey(key: String) = addKey(key, Handler.IDENTITY)

    fun loadFromFile() {
        if (!configFilePath.exists()) {
            // Create an empty config file
            Files.write(configFilePath, "{}".toByteArray())
        }

        importRawEntries(loadConfigRawEntries(configFilePath))
        isModified.set(false)
    }

    fun saveToFile() {
        if (!isModified.get()) {
            return
        }

        val rawEntries = exportRawEntries()
        val content = Gson().toJson(rawEntries)
        Files.write(configFilePath, content.toByteArray())
    }

    fun clone() = Config(chamber).apply {
        itemHandlers.putAll(this@Config.itemHandlers)
        entries.putAll(this@Config.entries)
        isModified.set(this@Config.isModified.get())
    }

    private fun convertRawToItem(key: String, value: String): Any? {
        val handler = itemHandlers[key] ?: throw InvalidConfigKeyException(key)
        return handler.reader.read(value)
    }

    private fun <T> convertItemToRaw(key: String, item: T?): String {
        @Suppress("UNCHECKED_CAST")
        val handler = itemHandlers[key] as ConfigItemHandler<T>?
            ?: throw InvalidConfigKeyException(key)

        return handler.writer.write(item)
    }

    private fun importRawEntries(rawEntries: Map<String, String>) {
        for ((key, value) in rawEntries) {
            set(key, convertRawToItem(key, value))
        }

        isModified.set(true)
    }

    private fun exportRawEntries(): MutableMap<String, String> {
        val rawEntries: MutableMap<String, String> = mutableMapOf()

        for ((key, value) in entries) {
            rawEntries[key] = convertItemToRaw(key, value)
        }

        return rawEntries
    }

    private fun loadConfigRawEntries(configFilePath: Path): Map<String, String> {
        val content = Files.readString(configFilePath)
        val type = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(content, type)
    }

    object Handler {
        val IDENTITY = ConfigItemHandler({ it }, { it ?: "" })
        val INT = ConfigItemHandler({ it.toInt() }, { it?.toString() ?: "0" })
        val LONG = ConfigItemHandler({ it.toLong() }, { it?.toString() ?: "0" })
        val FLOAT =
            ConfigItemHandler({ it.toFloat() }, { it?.toString() ?: "0" })
        val DOUBLE =
            ConfigItemHandler({ it.toDouble() }, { it?.toString() ?: "0" })
        val BOOLEAN =
            ConfigItemHandler({ it.toBoolean() }, { it?.toString() ?: "false" })
    }
}

fun interface ConfigItemReader<T> {
    fun read(value: String): T?
}

fun interface ConfigItemWriter<T> {
    fun write(item: T?): String
}

data class ConfigItemHandler<T>(
    val reader: ConfigItemReader<T>,
    val writer: ConfigItemWriter<T>,
)

class InvalidConfigKeyException(key: String) :
    RuntimeException("Invalid config key: $key")
