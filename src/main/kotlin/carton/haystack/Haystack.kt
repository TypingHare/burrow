package burrow.carton.haystack

import burrow.carton.haystack.command.*
import burrow.carton.hoard.Entry
import burrow.carton.hoard.HoardPair
import burrow.common.converter.StringConverterPair
import burrow.kernel.Burrow
import burrow.kernel.config.Config
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies
import java.nio.file.Files
import java.nio.file.Path

@Furniture(
    version = Burrow.VERSION,
    description = "Manages pairs of relative paths (names) and absolute paths.",
    type = Furniture.Type.COMPONENT
)
@RequiredDependencies(Dependency(HoardPair::class, Burrow.VERSION))
class Haystack(renovator: Renovator) : Furnishing(renovator) {
    override fun prepareConfig(config: Config) {
        registerConfigKey(ConfigKey.PATH, StringConverterPairs.STRING_LIST)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.PATH, mutableListOf<String>())
        config[HoardPair.ConfigKey.KEY_NAME] = EntryKey.NAME
        config[HoardPair.ConfigKey.VALUE_NAME] = EntryKey.ABSOLUTE_PATH
        config[HoardPair.ConfigKey.ALLOW_DUPLICATE_KEYS] = false
    }

    override fun assemble() {
        registerCommand(NewCommand::class)
        registerCommand(DelCommand::class)
        registerCommand(InfoCommand::class)
        registerCommand(PathListCommand::class)
        registerCommand(PathAddCommand::class)
        registerCommand(PathRemoveCommand::class)
        registerCommand(ScanCommand::class)
    }

    fun createEntry(name: String, absolutePath: String): Entry =
        use(HoardPair::class).createEntry(name, absolutePath)

    @Throws(
        AbsolutePathNotExistException::class,
        MultipleAbsolutePathsException::class
    )
    fun createEntry(name: String): Entry {
        val candidateAbsolutePaths = getCandidateAbsolutePaths(name)
        if (candidateAbsolutePaths.isEmpty()) {
            throw AbsolutePathNotExistException(name)
        }

        if (candidateAbsolutePaths.size > 2) {
            throw MultipleAbsolutePathsException(
                name,
                candidateAbsolutePaths
            )
        }

        val absolutePath = candidateAbsolutePaths[0]
        return createEntry(name, absolutePath.toString())
    }

    fun getEntry(name: String): Entry {
        return use(HoardPair::class)
            .getEntries(name)
            .firstOrNull() ?: throw EntryNotFoundException(name)
    }

    fun getPathList(): MutableList<String> = config.getNotNull(ConfigKey.PATH)

    private fun getCandidateAbsolutePaths(relativePath: String): List<Path> {
        return getPathList().mapNotNull { path ->
            Path.of(path).resolve(relativePath).toAbsolutePath()
                .takeIf { Files.exists(it) }
        }
    }

    companion object {
        fun extractAbsolutePath(entry: Entry): String =
            entry.get<String>(EntryKey.ABSOLUTE_PATH)!!
    }

    object ConfigKey {
        const val PATH = "haystack.path"
    }

    object EntryKey {
        const val NAME = "name"
        const val ABSOLUTE_PATH = "absolute_path"
    }

    object Standard {
        const val PATH_DELIMITER = ":"
    }

    object StringConverterPairs {
        val STRING_LIST = StringConverterPair(
            {
                it.split(Standard.PATH_DELIMITER)
                    .filter(String::isNotBlank)
                    .toMutableList()
            },
            { it?.joinToString(Standard.PATH_DELIMITER).orEmpty() }
        )
    }
}

class AbsolutePathNotExistException(name: String) :
    RuntimeException("Absolute path not exist for name: $name")

class MultipleAbsolutePathsException(
    name: String,
    val candidateAbsolutePaths: List<Path>
) : RuntimeException("Multiple absolute paths found: $name")

class EntryNotFoundException(name: String) :
    RuntimeException("Entry not found with name: $name")