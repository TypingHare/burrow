package burrow.carton.shell

import burrow.carton.shell.command.ShellContentCommand
import burrow.carton.shell.command.ShellNewCommand
import burrow.carton.shell.command.ShellPathCommand
import burrow.kernel.Burrow
import burrow.kernel.config.Config
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Furniture
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

@Furniture(
    version = Burrow.VERSION,
    description = "Allows developers to create shell files.",
    type = Furniture.Type.COMPONENT
)
class Shell(renovator: Renovator) : Furnishing(renovator) {
    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.BIN_SHELL)
        config.addKey(ConfigKey.BIN_NAME)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.BIN_SHELL, DEFAULT.BIN_SHELL)
        config.setIfAbsent(ConfigKey.BIN_NAME, chamber.name)
    }

    override fun assemble() {
        registerCommand(ShellNewCommand::class)
        registerCommand(ShellPathCommand::class)
        registerCommand(ShellContentCommand::class)
    }

    private fun getBinShell(): String = config.getNotNull(ConfigKey.BIN_NAME)

    private fun getBinName(): String = config.getNotNull(ConfigKey.BIN_NAME)

    fun getBinFile(): Path =
        burrow.getPath().resolve(Burrow.BIN_DIR).resolve(getBinName())

    fun createShellFile(content: String) {
        createShellFile(getBinFile(), content)
    }

    fun getDefaultShellContent(): String {
        val shell = getBinShell()
        val chamberName = chamber.name

        return "#! $shell\n\nburrow $chamberName \"$@\""
    }

    private fun createShellFile(path: Path, content: String) {
        try {
            Files.write(path, content.toByteArray())
            @Suppress("SpellCheckingInspection")
            val permissions = PosixFilePermissions.fromString("rwxr-xr-x")
            Files.setPosixFilePermissions(path, permissions)
        } catch (ex: IOException) {
            throw CreateShellFileException(path)
        }
    }

    object ConfigKey {
        const val BIN_SHELL = "bin.shell"
        const val BIN_NAME = "bin.name"
    }

    object DEFAULT {
        const val BIN_SHELL = "/bin/zsh"
    }
}

class CreateShellFileException(path: Path) :
    RuntimeException("Unable to create shell file: $path")