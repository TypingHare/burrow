package burrow.carton.hay

import burrow.carton.hay.command.*
import burrow.carton.hoard.Entry
import burrow.carton.hoard.HoardPair
import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.config.Config
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.annotation.DependsOn
import burrow.kernel.furnishing.annotation.Furniture
import java.nio.file.Files
import java.nio.file.Path

@Furniture(
    version = Burrow.VERSION.NAME,
    description = "Manages pairs of relative paths and absolute paths.",
    type = Furniture.Type.COMPONENT
)
@DependsOn(HoardPair::class)
class Hay(chamber: Chamber) : Furnishing(chamber) {
    override fun prepareConfig(config: Config) {
        config.addKey(
            ConfigKey.PATH,
            { it.split(Standard.PATH_DELIMITER).filter(String::isNotBlank) },
            { it?.joinToString(Standard.PATH_DELIMITER).orEmpty() }
        )
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.PATH, mutableListOf<String>())
        config[HoardPair.ConfigKey.KEY_NAME] = EntryKey.RELATIVE_PATH
        config[HoardPair.ConfigKey.VALUE_NAME] = EntryKey.ABSOLUTE_PATH
        config[HoardPair.ConfigKey.ALLOW_DUPLICATE_KEYS] = false
    }

    override fun assemble() {
        registerCommand(NewCommand::class)
        registerCommand(InfoCommand::class)
        registerCommand(PathCommand::class)
        registerCommand(PathAddCommand::class)
        registerCommand(PathRemoveCommand::class)
    }

    private fun getCandidateAbsolutePaths(relativePath: String): List<Path> {
        return getPaths().mapNotNull { path ->
            Path.of(path).resolve(relativePath).toAbsolutePath()
                .takeIf { Files.exists(it) }
        }
    }

    fun createEntry(
        relativePath: String,
        absolutePath: String
    ): Entry {
        return use(HoardPair::class).createEntry(relativePath, absolutePath)
    }

    @Throws(
        AbsolutePathNotExistException::class,
        MultipleAbsolutePathsException::class
    )
    fun createEntry(relativePath: String): Entry {
        val candidateAbsolutePaths = getCandidateAbsolutePaths(relativePath)
        if (candidateAbsolutePaths.isEmpty()) {
            throw AbsolutePathNotExistException(relativePath)
        }

        if (candidateAbsolutePaths.size > 2) {
            throw MultipleAbsolutePathsException(
                relativePath,
                candidateAbsolutePaths
            )
        }

        val absolutePath = candidateAbsolutePaths[0]
        return createEntry(relativePath, absolutePath.toString())
    }

    fun getEntry(relativePath: String): Entry {
        return use(HoardPair::class)
            .getEntries(relativePath)
            .firstOrNull() ?: throw EntryNotFoundException(relativePath)
    }

    fun getPaths(): List<String> = config.getNotNull(ConfigKey.PATH)

    companion object {
        fun extractAbsolutePath(entry: Entry): String =
            entry.get<String>(EntryKey.ABSOLUTE_PATH)!!
    }

    object ConfigKey {
        const val PATH = "hay.path"
    }

    object EntryKey {
        const val RELATIVE_PATH = "relative_path"
        const val ABSOLUTE_PATH = "absolute_path"
    }

    object Standard {
        const val PATH_DELIMITER = ":"
    }
}

class AbsolutePathNotExistException(relativePath: String) :
    RuntimeException("Absolute path not exist for: $relativePath")

class MultipleAbsolutePathsException(
    relativePath: String,
    val candidateAbsolutePaths: List<Path>
) : RuntimeException("Multiple absolute paths: $relativePath")

class EntryNotFoundException(relativePath: String) :
    RuntimeException("Entry not found: $relativePath")