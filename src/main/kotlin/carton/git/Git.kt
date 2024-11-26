package burrow.carton.git

import burrow.carton.cradle.Cradle
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
    description = "Manage git repositories easily.",
    type = Furniture.Type.MAIN
)
@DependsOn(HoardPair::class, Cradle::class)
class Git(chamber: Chamber) : Furnishing(chamber) {
    override fun prepareConfig(config: Config) {
        config.addKey(
            ConfigKey.PATH,
            { it.split(Standard.PATH_DELIMITER).filter(String::isNotBlank) },
            {
                println(it)
                println(it?.joinToString(Standard.PATH_DELIMITER).orEmpty())
                it?.joinToString(Standard.PATH_DELIMITER).orEmpty()
            }
        )
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.PATH, Default.PATH)
        config.setIfAbsent(HoardPair.ConfigKey.KEY_NAME, EntryKey.RELATIVE_PATH)
        config.setIfAbsent(
            HoardPair.ConfigKey.VALUE_NAME,
            EntryKey.ABSOLUTE_PATH
        )
    }

    override fun assemble() {
        registerCommand(NewCommand::class)
        registerCommand(InfoCommand::class)
    }

    private fun getCandidateAbsolutePaths(path: String): List<Path> {
        val paths = config.get<List<String>>(ConfigKey.PATH)!!
        val absolutePaths = mutableListOf<Path>()
        paths.forEach {
            val absolutePath = Path.of(it).resolve(path).toAbsolutePath()
            if (Files.exists(absolutePath)) {
                absolutePaths.add(absolutePath)
            }
        }

        return absolutePaths
    }

    private fun createRepositoryEntry(
        relativePath: String,
        absolutePath: String
    ): Entry {
        return use(HoardPair::class).createEntry(relativePath, absolutePath)
    }

    @Throws(
        GitRepositoryNotFoundException::class,
        MultipleGitRepositoriesException::class
    )
    fun createRepositoryEntry(path: String): Entry {
        val candidateAbsolutePaths = getCandidateAbsolutePaths(path)
        if (candidateAbsolutePaths.isEmpty()) {
            throw GitRepositoryNotFoundException(path)
        }

        if (candidateAbsolutePaths.size > 2) {
            throw MultipleGitRepositoriesException(candidateAbsolutePaths)
        }

        val absolutePath = candidateAbsolutePaths[0]
        return createRepositoryEntry(path, absolutePath.toString())
    }

    object ConfigKey {
        const val PATH = "git.path"
    }

    object Default {
        const val PATH = ""
    }

    object EntryKey {
        const val RELATIVE_PATH = "relative_path"
        const val ABSOLUTE_PATH = "absolute_path"
    }

    object Standard {
        const val PATH_DELIMITER = ":"
    }
}

class GitRepositoryNotFoundException(path: String) :
    RuntimeException("Git repository not found: $path")

class MultipleGitRepositoriesException(val absolutePaths: List<Path>) :
    RuntimeException("Multiple git repository found.")