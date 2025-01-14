package burrow.kernel.config

import burrow.common.converter.AnyStringConverterPairContainer
import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import burrow.kernel.path.Persistable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.exists

class Config(chamber: Chamber) : ChamberModule(chamber), Persistable,
    Cloneable {
    private val path = chamber.getPath().resolve(FILE_NAME)
    val converterPairContainer = AnyStringConverterPairContainer()
    val entries = mutableMapOf<String, Any?>()
    val isModified = AtomicBoolean(false)

    override fun getPath(): Path = path

    override fun save() {
        if (!isModified.get()) return

        Gson().toJson(exportRawEntries()).let {
            Files.write(path, it.toByteArray())
        }
    }

    override fun load() {
        when (path.exists()) {
            true -> importRawEntries(loadConfigRawEntries(path))
            else -> Files.write(path, "{}".toByteArray())
        }

        isModified.set(false)
    }

    public override fun clone(): Config = Config(chamber).apply {
        converterPairContainer.converterPairs.putAll(
            this@Config.converterPairContainer.converterPairs
        )
        entries.putAll(this@Config.entries)
        isModified.set(this@Config.isModified.get())
    }

    operator fun <T> get(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return entries[key] as T?
    }

    operator fun set(key: String, value: Any?) {
        if (!converterPairContainer.converterPairs.containsKey(key)) {
            throw InvalidConfigKeyException(key)
        }

        entries[key] = value
        isModified.set(true)
    }

    fun <T> getNotNull(key: String): T =
        get<T>(key) ?: throw ConfigValueIsNullException(key)

    fun setIfAbsent(key: String, value: Any?) {
        if (!entries.containsKey(key) || entries[key] == null) {
            set(key, value)
        }
    }

    private fun importRawEntries(rawEntries: Map<String, String>) {
        rawEntries.forEach { (key, value) ->
            set(key, converterPairContainer.toRight<Any>(key, value))
        }
        isModified.set(true)
    }

    private fun exportRawEntries(): MutableMap<String, String> {
        return entries.map { (key, value) ->
            key to converterPairContainer.toLeft(key, value)
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
}

class InvalidConfigKeyException(key: String) :
    RuntimeException("Invalid config key: $key")

class ConfigValueIsNullException(key: String) :
    RuntimeException("Config value associated is null: $key")