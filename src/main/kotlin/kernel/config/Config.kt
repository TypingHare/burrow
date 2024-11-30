package burrow.kernel.config

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

class Config(chamber: Chamber) : ChamberModule(chamber), Cloneable {
    private val file: File = chamber.rootDirPath.resolve(FILE_NAME).toFile()
    private val itemHandlers = mutableMapOf<String, ConfigItemHandler<*>>()
    private val entries = mutableMapOf<String, Any?>()
    private val isModified = AtomicBoolean(false)

    operator fun <T> get(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return entries[key] as T?
    }

    operator fun set(key: String, value: Any?) {
        entries[key] = value
        isModified.set(true)
    }

    fun <T> getNotNull(key: String): T = get<T>(key)!!

    fun setIfAbsent(key: String, value: Any) {
        if (!entries.containsKey(key) || entries[key] == null) {
            set(key, value)
        }
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
        if (!file.exists()) {
            Files.write(file.toPath(), "{}".toByteArray())
        } else {
            importRawEntries(loadConfigRawEntries(file.toPath()))
        }

        isModified.set(false)
    }

    fun saveToFile() {
        if (!isModified.get()) {
            return
        }

        val content = Gson().toJson(exportRawEntries())
        Files.write(file.toPath(), content.toByteArray())
    }

    override fun clone(): Config = Config(chamber).apply {
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
        rawEntries.forEach { (key, value) ->
            set(key, convertRawToItem(key, value))
        }
        isModified.set(true)
    }

    private fun exportRawEntries(): MutableMap<String, String> {
        return entries.map { (key, value) ->
            key to convertItemToRaw(key, value)
        }.toMap().toMutableMap()
    }

    private fun loadConfigRawEntries(configFilePath: Path): Map<String, String> {
        val content = Files.readString(configFilePath)
        val type = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(content, type)
    }

    companion object {
        const val FILE_NAME = "config.json"
    }

    object Handler {
        val IDENTITY = ConfigItemHandler({ it }, { it ?: "" })
        val INT = ConfigItemHandler({ it.toInt() }, { it?.toString() ?: "0" })
        val LONG = ConfigItemHandler({ it.toLong() }, { it?.toString() ?: "0" })
        val FLOAT =
            ConfigItemHandler({ it.toFloat() }, { it?.toString() ?: "0.0" })
        val DOUBLE =
            ConfigItemHandler({ it.toDouble() }, { it?.toString() ?: "0.0" })
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
