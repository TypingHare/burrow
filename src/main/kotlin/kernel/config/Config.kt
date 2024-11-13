package burrow.kernel.config

import java.util.concurrent.atomic.AtomicBoolean

class Config {
    companion object {
        val defaultConfigItemHandler = ConfigItemHandler(
            reader = { it },
            writer = { it ?: "" }
        )
    }

    private val itemHandlers = mutableMapOf<String, ConfigItemHandler<*>>()
    private val entries = mutableMapOf<String, Any?>()
    val isModified = AtomicBoolean(false)

    fun <T> get(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return entries[key] as T?
    }

    fun set(key: String, value: Any?) {
        entries[key] = value
        isModified.set(false)
    }

    fun setIfAbsent(key: String, value: Any) {
        if (key !in entries || entries[key] == null) set(key, value)
    }

    private fun addKey(key: String, handler: ConfigItemHandler<*>) {
        itemHandlers[key] = handler
    }

    fun <T> addKey(
        key: String,
        reader: ConfigItemReader<T>,
        writer: ConfigItemWriter<T>
    ) = addKey(key, ConfigItemHandler(reader, writer))

    fun addKey(key: String) = addKey(key, defaultConfigItemHandler)

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

    fun importRawEntries(rawEntries: Map<String, String>) {
        for ((key, value) in rawEntries) {
            set(key, convertRawToItem(key, value))
        }

        isModified.set(true)
    }

    fun exportRawEntries(): MutableMap<String, String> {
        val rawEntries: MutableMap<String, String> = mutableMapOf()

        for ((key, value) in entries) {
            rawEntries[key] = convertItemToRaw(key, value)
        }

        return rawEntries
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
