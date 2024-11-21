package burrow.carton.hoard

import burrow.carton.hoard.Entry.Key
import burrow.carton.hoard.command.*
import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.event.Event
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.annotation.Furniture
import burrow.kernel.palette.Highlight
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.exists

@Furniture(
    version = Burrow.VERSION.NAME,
    description = "Hoard stores entries.",
    type = Furniture.Type.COMPONENT
)
class Hoard(chamber: Chamber) : Furnishing(chamber) {
    private val hoardFilePath: Path =
        chamber.rootPath.resolve(Standard.HOARD_FILE)
    val entryStore = mutableListOf<Entry?>()
    val maxId = AtomicInteger(0)
    val size = AtomicInteger(0)
    val saveWhenDiscard = AtomicBoolean(false)

    override fun assemble() {
        // Entry relevant
        registerCommand(NewCommand::class)
        registerCommand(ExistCommand::class)
        registerCommand(EntryCommand::class)
        registerCommand(DelCommand::class)

        // Properties relevant
        registerCommand(SetCommand::class)
        registerCommand(UnsetCommand::class)
        registerCommand(PropCommand::class)

        // Multiple entries
        registerCommand(EntriesCommand::class)
        registerCommand(CountCommand::class)
    }

    override fun launch() {
        loadFromHoardFile(hoardFilePath)
    }

    override fun discard() {
        if (saveWhenDiscard.get()) {
            saveToHoardFile(hoardFilePath)
        }
    }

    private fun loadFromHoardFile(hoardFilePath: Path) {
        if (!hoardFilePath.exists()) {
            createHoardFile(hoardFilePath)
        }

        try {
            val content = Files.readString(hoardFilePath)
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val entries: List<Map<String, String>> =
                Gson().fromJson(content, type)
            entries.forEach { restore(it) }
        } catch (ex: IOException) {
            throw RuntimeException("Failed to load hoard: $hoardFilePath", ex)
        }
    }

    private fun saveToHoardFile(hoardFilePath: Path) {
        try {
            val entryPropertyList = entryStore.filterNotNull()
                .map { return@map it.props }
            val content = Gson().toJson(entryPropertyList)
            Files.write(hoardFilePath, content.toByteArray())
        } catch (ex: IOException) {
            throw RuntimeException("Failed to save hoard: $hoardFilePath", ex)
        }
    }

    private fun restore(properties: Map<String, String>) {
        val id = properties[Key.ID]?.toInt()
            ?: throw IllegalArgumentException("ID is required")
        if (exists(id)) throw DuplicateIdException(id)

        val entry = Entry(id, properties.toMutableMap())
        properties.forEach { (k, v) -> entry.set(k, v) }
        affairManager.post(EntryRestoreEvent(entry))

        while (entryStore.size <= id) entryStore.add(null)
        entryStore[id] = entry
        maxId.updateAndGet { maxOf(it, id) }

        size.incrementAndGet()
    }

    fun create(properties: Map<String, String>): Entry {
        val id: Int = maxId.incrementAndGet()

        val entry = Entry(id, properties.toMutableMap())
        properties.forEach { (k, v) -> entry.set(k, v) }
        affairManager.post(EntryRestoreEvent(entry))

        for (i in entryStore.size..id) entryStore.add(null)
        entryStore[id] = entry
        size.incrementAndGet()

        saveWhenDiscard.set(true)
        return entry
    }

    fun delete(id: Int): Entry {
        val entry: Entry = entryStore[id] ?: throw EntryNotFoundException(id)

        affairManager.post(EntryDeleteEvent(entry))
        entryStore[id] = null
        size.decrementAndGet()
        affairManager.post(EntryDeleteEvent(entry))

        saveWhenDiscard.set(true)
        return entry
    }

    fun convertStoreToProperties(entry: Entry): Map<String, String> {
        val properties = mutableMapOf<String, String>()
        for ((key, value) in entry.store) {
            if (key == Key.ID) {
                continue
            }
            properties[key] = value.toString()
        }
        affairManager.post(EntryStringifyEvent(entry, properties))

        return properties
    }

    fun exists(id: Int): Boolean =
        id >= 0 && id < entryStore.size && entryStore[id] != null

    fun setProperties(entry: Entry, properties: Map<String, String>) {
        properties.forEach { (k, v) ->
            entry.setProp(k, v)
            entry.set(k, v)
        }
        affairManager.post(EntrySetPropertiesEvent(entry, properties))
        saveWhenDiscard.set(true)
    }

    fun unsetProperties(entry: Entry, keys: List<String>) {
        keys.forEach { entry.unset(it) }
        affairManager.post(EntryUnsetPropertiesEvent(entry, keys))
        saveWhenDiscard.set(true)
    }

    @Throws(EntryNotFoundException::class)
    operator fun get(id: Int): Entry {
        try {
            return entryStore[id] ?: throw EntryNotFoundException(id)
        } catch (ex: IndexOutOfBoundsException) {
            throw EntryNotFoundException(id)
        }
    }

    @Throws(RuntimeException::class)
    private fun createHoardFile(hoardFilePath: Path) {
        try {
            Files.write(hoardFilePath, "[]".toByteArray())
        } catch (ex: IOException) {
            throw RuntimeException("Failed to create hoard: $hoardFilePath", ex)
        }
    }

    object Standard {
        const val HOARD_FILE = "hoard.json"
    }

    object Highlights {
        val KEY = Highlight(81, 0, 0)
        val VALUE = Highlight(222, 0, 0)
        val NULL = Highlight(196, 0, 0)

        val ID = Highlight(41, 0, 0)
        val BRACE = Highlight(214, 0, 0)
    }
}

class EntryNotFoundException(id: Int) :
    RuntimeException("Entry with such ID not found: $id")

class DuplicateIdException(id: Int) :
    RuntimeException("Duplicate entry id: $id")

class EntryRestoreEvent(val entry: Entry) : Event()

class EntryCreateEvent(val entry: Entry) : Event()

class EntryDeleteEvent(val entry: Entry) : Event()

class EntrySetPropertiesEvent(
    val entry: Entry,
    val props: Map<String, String>
) : Event()

class EntryUnsetPropertiesEvent(
    val entry: Entry,
    val keys: List<String>
) : Event()

class EntryStringifyEvent(
    val entry: Entry,
    val props: MutableMap<String, String>
) : Event()