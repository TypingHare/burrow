package burrow.carton.cradle

import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.command.Environment
import burrow.kernel.config.Config
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.annotation.Furniture
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter


@Furniture(
    version = Burrow.VERSION.NAME,
    description = "Cradle allows users to create a child process and execute a command.",
    type = Furniture.Type.COMPONENT
)
class Cradle(chamber: Chamber) : Furnishing(chamber) {
    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.SHELL_PATH)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.SHELL_PATH, Default.SHELL_PATH)
    }

    override fun assemble() {
        registerCommand(ExecCommand::class)
    }

    private fun getProcessBuilder(
        command: String,
        environment: Environment,
    ): ProcessBuilder {
        val shellPath = config.get<String>(ConfigKey.SHELL_PATH)
        val processBuilder = ProcessBuilder(shellPath, "-c", command)
        val workingDirectory = environment.workingDirectory
        processBuilder.directory(File(workingDirectory))

        return processBuilder
    }

    fun executeCommand(
        command: String,
        environment: Environment,
        stdout: PrintWriter,
        stderr: PrintWriter
    ): Int {
        val processBuilder = getProcessBuilder(command, environment)
        val process = processBuilder.start()

        var line: String
        val stdoutReader =
            BufferedReader(InputStreamReader(process.inputStream))
        while ((stdoutReader.readLine().also { line = it }) != null) {
            stdout.println(line)
        }

        val stderrReader =
            BufferedReader(InputStreamReader(process.errorStream))
        while ((stderrReader.readLine().also { line = it }) != null) {
            stderr.println(line)
        }

        return process.waitFor()
    }

    object ConfigKey {
        const val SHELL_PATH = "cradle.shell_path"
    }

    object Default {
        const val SHELL_PATH = "/bin/bash"
    }
}