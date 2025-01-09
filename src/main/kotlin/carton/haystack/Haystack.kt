package burrow.carton.haystack

import burrow.carton.haystack.command.*
import burrow.carton.hoard.Entry
import burrow.carton.hoard.HoardPair
import burrow.common.converter.ConverterPair
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
    description = "Manages pairs of relative paths and absolute paths.",
    type = Furniture.Type.COMPONENT
)
@RequiredDependencies(Dependency(HoardPair::class, Burrow.VERSION))
class Haystack(renovator: Renovator) : Furnishing(renovator) {
    override fun prepareConfig(config: Config) {
        config.addKey(
            ConfigKey.PATH, ConverterPair(
                {
                    it.split(Standard.PATH_DELIMITER).filter(String::isNotBlank)
                },
                { it?.joinToString(Standard.PATH_DELIMITER).orEmpty() }
            ))
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
        registerCommand(PathListCommand::class)
        registerCommand(PathAddCommand::class)
        registerCommand(PathRemoveCommand::class)
        registerCommand(ScanCommand::class)
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

    private fun getCandidateAbsolutePaths(relativePath: String): List<Path> {
        return getPaths().mapNotNull { path ->
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
        const val RELATIVE_PATH = "relative_path"
        const val ABSOLUTE_PATH = "absolute_path"
    }

    object Standard {
        const val PATH_DELIMITER = ":"
    }
}

class AbsolutePathNotExistException(relativePath: String) :
    RuntimeException("Absolute path not exist for relative path: $relativePath")

class MultipleAbsolutePathsException(
    relativePath: String,
    val candidateAbsolutePaths: List<Path>
) : RuntimeException("Multiple absolute paths found: $relativePath")

class EntryNotFoundException(relativePath: String) :
    RuntimeException("Entry not found with relative path: $relativePath")