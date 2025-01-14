package burrow.carton.hoard

import burrow.carton.hoard.command.*
import burrow.carton.hoard.command.backup.BackupCommand
import burrow.carton.hoard.command.backup.BackupDeleteCommand
import burrow.carton.hoard.command.backup.BackupListCommand
import burrow.carton.hoard.command.backup.BackupRestoreCommand
import burrow.common.event.Event
import burrow.kernel.Burrow
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.path.Persistable
import java.nio.file.Path

@Furniture(
    version = Burrow.VERSION,
    description = "Hoard stores entries.",
    type = Furniture.Type.COMPONENT
)
class Hoard(renovator: Renovator) : Furnishing(renovator), Persistable {
    /**
     * The only storage that is managed by the Hoard furnishing. Users can
     * create their own storages.
     */
    val storage =
        Storage(chamber.getPath().resolve(DEFAULT_STORAGE_FILE), courier)

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

    override fun getPath(): Path = storage.getPath()

    override fun launch() {
        load()
    }

    override fun discard() {
        if (storage.hasUpdated.get()) {
            save()
        }
    }

    override fun save() = storage.save()

    override fun load() = storage.load()

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

    companion object {
        const val DEFAULT_STORAGE_FILE = "storage.json"
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

class EntryUpdateEvent(val entry: Entry, val keys: List<String>) : Event()

class FormatEntryEvent(
    val entry: Entry,
    val props: MutableMap<String, String>
) : Event()