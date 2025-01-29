package burrow.carton.hoard

import burrow.common.converter.AnyStringConverterPairContainer
import burrow.common.converter.StringConverterPair
import burrow.common.event.EventBus
import burrow.kernel.path.Persistable
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.exists

class Storage(
    private val path: Path,
    val courier: EventBus
) : Persistable {
    val entryStore = mutableListOf<Entry?>()
    val converterPairContainer = AnyStringConverterPairContainer()
    val maxId = AtomicInteger(0)
    val size = AtomicInteger(0)
    val hasUpdated = AtomicBoolean(false)

    override fun getPath(): Path = path

    override fun save() = saveTo(path)

    @Throws(IOException::class, JsonSyntaxException::class)
    override fun load() {
        if (!path.exists()) {
            createNewHoardFile()
        }

        try {
            val content = Files.readString(path)
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val entries: List<Map<String, String>> =
                Gson().fromJson(content, type)
            entries.forEach { restore(it) }
        } catch (ex: IOException) {
            throw LoadHoardException(path, ex)
        }
    }

    fun addMapping(
        key: String,
        @Suppress("UNCHECKED_CAST") converterPair: StringConverterPair<*> =
            StringConverterPair.IDENTITY as StringConverterPair<Any>
    ) {
        @Suppress("UNCHECKED_CAST")
        converterPairContainer.add(
            key,
            converterPair as StringConverterPair<Any>
        )
    }

    /**
     * Restores an entry from properties.
     */
    private fun restore(properties: Map<String, String>) {
        val id = properties[Entry.Key.ID]?.toInt()
            ?: throw IllegalArgumentException("ID is required")
        if (exists(id)) throw DuplicateIdException(id)

        val entry = Entry(id, this).apply { set(properties) }
        courier.post(EntryRestoreEvent(entry))

        while (entryStore.size <= id) entryStore.add(null)
        entryStore[id] = entry
        maxId.updateAndGet { maxOf(it, id) }

        size.incrementAndGet()
    }

    /**
     * Creates a new entry.
     */
    fun create(properties: Map<String, String>): Entry {
        val id: Int = maxId.incrementAndGet()

        val entry = Entry(id, this).apply { set(properties) }
        courier.post(EntryCreateEvent(entry))

        for (i in entryStore.size..id) entryStore.add(null)
        entryStore[id] = entry
        size.incrementAndGet()

        hasUpdated.set(true)
        return entry
    }

    /**
     * Deletes an entry.
     */
    fun delete(id: Int): Entry {
        val entry: Entry = entryStore[id] ?: throw EntryNotFoundException(id)

        courier.post(EntryDeleteEvent(entry))
        entryStore[id] = null
        size.decrementAndGet()
        courier.post(EntryDeleteEvent(entry))

        if (id == maxId.get()) {
            for (i in id downTo 0) {
                if (entryStore[i] != null) {
                    maxId.set(i)
                }
            }
        }

        hasUpdated.set(true)
        return entry
    }

    /**
     * Checks if an entry exists.
     */
    fun exists(id: Int): Boolean =
        id > 0 && id < entryStore.size && entryStore[id] != null

    /**
     * Formats the values in the entry store.
     */
    fun formatStore(entry: Entry): Map<String, String> {
        val properties = mutableMapOf<String, String>()
        for ((key, value) in entry.store) {
            if (key == Entry.Key.ID) {
                continue
            }
            properties[key] = value.toString()
        }
        courier.post(FormatEntryEvent(entry, properties))

        return properties
    }

    @Throws(EntryNotFoundException::class)
    operator fun get(id: Int): Entry {
        try {
            return entryStore[id] ?: throw EntryNotFoundException(id)
        } catch (ex: IndexOutOfBoundsException) {
            throw EntryNotFoundException(id)
        }
    }

    fun getAllEntries(): List<Entry> =
        entryStore.filterNotNull()

    fun saveTo(path: Path) {
        try {
            val entryPropertyList = entryStore
                .filterNotNull()
                .map { return@map it.toProperties() }
            val content = Gson().toJson(entryPropertyList)
            Files.write(path, content.toByteArray())
        } catch (ex: IOException) {
            throw RuntimeException("Failed to save hoard: $path", ex)
        }
    }

    fun operate(id: Int, callback: Entry.() -> Unit) {
        get(id).apply(callback)
    }

    fun operateIfExists(id: Int, callback: Entry.() -> Unit) {
        entryStore[id]?.apply(callback)
    }

    fun clear() {
        entryStore.clear()
        maxId.set(0)
        size.set(0)
        hasUpdated.set(true)
    }

    private fun createNewHoardFile() {
        try {
            Files.write(path, "[]".toByteArray())
        } catch (ex: IOException) {
            throw CreateHoardException(path, ex)
        }
    }
}