package burrow.furnishing.hoard

import burrow.furnishing.hoard.Entry.Key
import burrow.furnishing.hoard.command.EntryCommand
import burrow.furnishing.hoard.command.ExistCommand
import burrow.furnishing.hoard.command.NewCommand
import burrow.kernel.chamber.Chamber
import burrow.kernel.event.Event
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.Furniture
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
    version = "0.0.0",
    description = "Hoard stores entries.",
    type = Furniture.Type.COMPONENT
)
class Hoard(chamber: Chamber) : Furnishing(chamber) {
    private val hoardFilePath: Path =
        chamber.rootPath.resolve(Standard.HOARD_FILE)
    private val entryStore = mutableListOf<Entry?>()
    private var maxId = AtomicInteger(0)
    private val size = AtomicInteger(0)
    private val saveWhenDiscard = AtomicBoolean(true)

    override fun assemble() {
        registerCommand(NewCommand::class)
        registerCommand(ExistCommand::class)
        registerCommand(EntryCommand::class)
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
            entries.forEach { register(it) }
        } catch (ex: IOException) {
            throw RuntimeException("Failed to load hoard: $hoardFilePath", ex)
        }
    }

    private fun saveToHoardFile(hoardFilePath: Path) {
        try {
            val entryPropertyList = entryStore.filterNotNull()
                .map { return@map it.properties }
            val content = Gson().toJson(entryPropertyList)
            Files.write(hoardFilePath, content.toByteArray())
        } catch (ex: IOException) {
            throw RuntimeException("Failed to save hoard: $hoardFilePath", ex)
        }
    }

    private fun register(properties: Map<String, String>) {
        val id = properties[Key.ID]?.toInt()
            ?: throw IllegalArgumentException("ID is required")
        if (exists(id)) throw DuplicateIdException(id)

        affairManager.post(EntryPreRegisterEvent(this, properties))
        val entry = Entry(id, properties.toMutableMap())
        affairManager.post(EntryPostRegisterEvent(this, entry))

        while (entryStore.size <= id) entryStore.add(null)
        entryStore[id] = entry
        maxId.updateAndGet { maxOf(it, id) }

        size.incrementAndGet()
    }

    fun create(properties: Map<String, String>): Entry {
        val id: Int = maxId.incrementAndGet()

        affairManager.post(EntryPreCreateEvent(this, properties))
        val entry = Entry(id, properties.toMutableMap())
        affairManager.post(EntryPostRegisterEvent(this, entry))

        for (i in entryStore.size..id) entryStore.add(null)
        entryStore[id] = entry
        size.incrementAndGet()

        return entry
    }

    fun delete(id: Int): Entry {
        val entry: Entry = entryStore[id] ?: throw EntryNotFoundException(id)

        affairManager.post(EntryPreDeleteEvent(this, entry))
        entryStore[id] = null
        size.decrementAndGet()
        affairManager.post(EntryPostDeleteEvent(this, entry))

        return entry
    }

    fun stringify(entry: Entry): String {
        val properties = mutableMapOf<String, String>()
        affairManager.post(EntryStringifyEvent(entry, properties))

        return properties.toString()
    }

    fun exists(id: Int): Boolean =
        id >= 0 && id < entryStore.size && entryStore[id] != null

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
}


class EntryNotFoundException(id: Int) :
    RuntimeException("Entry with such ID not found: $id")

class DuplicateIdException(id: Int) :
    RuntimeException("Duplicate entry id: $id")

class EntryPreRegisterEvent(
    val hoard: Hoard,
    val properties: Map<String, String>
) : Event()

class EntryPostRegisterEvent(
    val hoard: Hoard,
    val entry: Entry
) : Event()

class EntryPreCreateEvent(
    val hoard: Hoard,
    val properties: Map<String, String>
) : Event()

class EntryPostCreateEvent(
    val hoard: Hoard,
    val entry: Entry
) : Event()

class EntryPreDeleteEvent(
    val hoard: Hoard,
    val entry: Entry
) : Event()

class EntryPostDeleteEvent(
    val hoard: Hoard,
    val entry: Entry
) : Event()

class EntryStringifyEvent(
    val entry: Entry,
    val properties: MutableMap<String, String>
) : Event()