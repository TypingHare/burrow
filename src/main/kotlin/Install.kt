package burrow

import burrow.common.resource.copyResourceFromJar
import burrow.kernel.Burrow
import burrow.kernel.Burrow.Companion.BIN_DIR
import burrow.kernel.Burrow.Companion.getBurrowRootPath
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.ExitCode
import burrow.kernel.terminal.Option
import burrow.kernel.terminal.Parameters
import picocli.CommandLine
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import java.util.concurrent.Callable
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    System.setProperty("slf4j.internal.verbosity", "WARN")
    CommandLine(Install()).execute(*args)
}

@BurrowCommand(
    name = "install",
    version = [Burrow.VERSION],
    header = ["Install Burrow."]
)
class Install : Callable<Int> {
    @Parameters(
        index = "0",
        description = ["The path of the burrow.jar file."]
    )
    private var burrowJarPathString: String = ""

    @Option(
        names = ["--force", "-f"],
        description = ["Force installation required."],
    )
    private var shouldBeForce = false

    @OptIn(ExperimentalPathApi::class)
    override fun call(): Int {
        val rootPath = getBurrowRootPath()
        val libsPath = rootPath.resolve(Burrow.LIBS_DIR)
        val chambersPath = rootPath.resolve(ChamberShepherd.CHAMBERS_DIR)
        if (!shouldBeForce && chambersPath.exists()) {
            println("It seems like that you have installed Burrow. To reinstall, append the '--force' option.")
            exitProcess(ExitCode.USAGE)
        }

        Files.createDirectories(libsPath)

        Files.copy(
            Path.of(burrowJarPathString),
            libsPath.resolve("burrow.jar"),
            StandardCopyOption.REPLACE_EXISTING
        )

        chambersPath.deleteRecursively()
        initializeChambers(chambersPath)

        val binPath = rootPath.resolve(BIN_DIR)
        binPath.deleteRecursively()
        initializeBin(binPath)
        binPath.toFile().listFiles()?.forEach { file ->
            setFilePermissionsTo755(file.toPath())
        }

        println("\uD83D\uDC30 Installation Complete! \uD83D\uDD73\uFE0F")
        println()
        println("To complete setup, add these lines to your shell profile (.bashrc, .zshrc, etc.):")
        println()
        println("   export BURROW_HOME=\"\$HOME/.burrow\"")
        println("   export PATH=\"\$PATH:\$BURROW_HOME/bin\"")
        println()
        println("After saving, either:")
        println("  • Start a new terminal session, or")
        println("  • Run 'source ~/.bashrc' (or your profile file)")
        println()
        println("To verify installation, run:")
        println("    burrow --help")
        println()
        println("If you see the root chamber information, Burrow is ready to use!")

        return ExitCode.OK
    }

    private fun initializeChambers(chambersPath: Path) {
        copyResourceFromJar("init/chambers", chambersPath)
    }

    private fun initializeBin(binPath: Path) {
        copyResourceFromJar("init/bin", binPath)
    }

    private fun setFilePermissionsTo755(path: Path) {
        @Suppress("SpellCheckingInspection") val permissions =
            PosixFilePermissions.fromString("rwxr-xr-x")
        Files.setPosixFilePermissions(path, permissions)
    }
}
