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
        config.addKey(ConfigKey.SHELL_INTERPRETER)
        config.addKey(ConfigKey.SHELL_NAME)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(
            ConfigKey.SHELL_INTERPRETER,
            DEFAULT.SHELL_INTERPRETER
        )
        config.setIfAbsent(ConfigKey.SHELL_NAME, chamber.name)
    }

    override fun assemble() {
        registerCommand(ShellNewCommand::class)
        registerCommand(ShellPathCommand::class)
        registerCommand(ShellContentCommand::class)
    }

    private fun getShellInterpreter(): String =
        config.getNotNull(ConfigKey.SHELL_INTERPRETER)

    private fun getShellName(): String = config.getNotNull(ConfigKey.SHELL_NAME)

    fun getShellFile(): Path =
        burrow.getPath().resolve(Burrow.BIN_DIR).resolve(getShellName())

    fun createShellFile(content: String) {
        createShellFile(getShellFile(), content)
    }

    fun getDefaultShellContent(): String {
        val shellInterpreter = getShellInterpreter()
        val chamberName = chamber.name

        return """
            #! $shellInterpreter
            
            burrow $chamberName "${'$'}@"
        """.trimIndent()
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
        const val SHELL_INTERPRETER = "shell.interpreter"
        const val SHELL_NAME = "shell.name"
    }

    object DEFAULT {
        const val SHELL_INTERPRETER = "/bin/zsh"
    }
}

class CreateShellFileException(path: Path) :
    RuntimeException("Unable to create shell file: $path")