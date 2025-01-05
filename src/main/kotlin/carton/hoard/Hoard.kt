package burrow.carton.hoard

import burrow.carton.hoard.command.*
import burrow.carton.hoard.command.backup.*
import burrow.kernel.Burrow
import burrow.kernel.event.Event
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.path.Persistable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.exists

@Furniture(
    version = Burrow.VERSION,
    description = "Hoard stores entries.",
    type = Furniture.Type.COMPONENT
)
class Hoard(renovator: Renovator) : Furnishing(renovator), Persistable {
    private val path = chamber.getPath().resolve(HOARD_FILE)
    private val entryStore = mutableListOf<Entry?>()
    val converterPairsContainer = StringConverterPairContainer()
    private val maxId = AtomicInteger(0)
    val size = AtomicInteger(0)
    private val saveWhenDiscard = AtomicBoolean(false)

    override fun assemble() {
        // Commands related to the hoard
        registerCommand(HoardSaveCommand::class)

        // Commands related to entries
        registerCommand(NewCommand::class)
        registerCommand(EntryCommand::class)
        registerCommand(ExistCommand::class)
        registerCommand(DelCommand::class)

        // Commands related to entry properties
        registerCommand(PropCommand::class)
        registerCommand(SetCommand::class)
        registerCommand(UnsetCommand::class)

        // Multiple entries and aggregation commands
        registerCommand(EntriesCommand::class)
        registerCommand(TableCommand::class)
        registerCommand(CountCommand::class)

        // Backup commands
        registerCommand(BackupCommand::class)
        registerCommand(BackupListCommand::class)
        registerCommand(BackupRestoreCommand::class)
        registerCommand(BackupDeleteCommand::class)
    }

    override fun launch() {
        load()
    }

    override fun discard() {
        if (saveWhenDiscard.get()) {
            save()
        }
    }

    override fun getPath(): Path = path

    override fun save() = saveTo(path)

    @Throws(IOException::class)
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

    /**
     * Restores an entry from properties.
     */
    private fun restore(properties: Map<String, String>) {
        val id = properties[Entry.Key.ID]?.toInt()
            ?: throw IllegalArgumentException("ID is required")
        if (exists(id)) throw DuplicateIdException(id)

        val entry = Entry(id, converterPairsContainer)
        properties.forEach { (k, v) -> entry.setWithLeft(k, v) }
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

        val entry = Entry(id, converterPairsContainer)
        properties.forEach { (k, v) -> entry.setWithLeft(k, v) }
        courier.post(EntryRestoreEvent(entry))

        for (i in entryStore.size..id) entryStore.add(null)
        entryStore[id] = entry
        size.incrementAndGet()

        saveWhenDiscard.set(true)
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

        saveWhenDiscard.set(true)
        return entry
    }

    /**
     * Checks if an entry exists.
     */
    fun exists(id: Int): Boolean =
        id > 0 && id < entryStore.size && entryStore[id] != null

    /**
     * Sets some properties for an entry.
     */
    fun setProperties(entry: Entry, properties: Map<String, String>) {
        properties.forEach { (k, v) ->
            entry.setWithLeft(k, v)
        }
        courier.post(EntrySetPropertiesEvent(entry, properties))
        saveWhenDiscard.set(true)
    }

    /**
     * Unset some properties from an entry.
     */
    fun unsetProperties(entry: Entry, keys: List<String>) {
        keys.forEach { entry.unset(it) }
        courier.post(EntryUnsetPropertiesEvent(entry, keys))
        saveWhenDiscard.set(true)
    }

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

    fun getBackupFileList(): List<BackupFile> {
        val filenames = chamber.getPath().toFile()
            .listFiles { it -> it.isFile() }
            ?.map { it.name } ?: emptyList()

        return filenames.mapNotNull { filename ->
            val pattern = Regex("""^hoard\.(\d+)\.json${'$'}""")
            val matcher = pattern.find(filename) ?: return@mapNotNull null
            BackupFile(filename, matcher.groupValues[1])
        }
    }

    private fun createNewHoardFile() {
        try {
            Files.write(path, "[]".toByteArray())
        } catch (ex: IOException) {
            throw CreateHoardException(path, ex)
        }
    }

    companion object {
        const val HOARD_FILE = "hoard.json"
        const val KEY_DELIMITER = ";"
    }
}

class LoadHoardException(path: Path, cause: Throwable) :
    RuntimeException("Failed to load hoard: $path", cause)

class CreateHoardException(path: Path, cause: Throwable) :
    RuntimeException("Failed to create hoard: $path", cause)

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

class FormatEntryEvent(
    val entry: Entry,
    val props: MutableMap<String, String>
) : Event()