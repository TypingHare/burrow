package burrow

import burrow.kernel.Burrow
import burrow.kernel.Burrow.Companion.BIN_DIR
import burrow.kernel.Burrow.Companion.getBurrowRootPath
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.ExitCode
import burrow.kernel.terminal.Option
import picocli.CommandLine
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
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
    @Option(
        names = ["--force", "-f"],
        description = ["Force installation required."],
    )
    private var shouldBeForce = false

    @OptIn(ExperimentalPathApi::class)
    override fun call(): Int {
        val rootPath = getBurrowRootPath()
        val chambersPath = rootPath.resolve(ChamberShepherd.CHAMBERS_DIR)
        if (!shouldBeForce && chambersPath.exists()) {
            println("It seems like that you have installed Burrow. To reinstall, append the '--force' option.")
            exitProcess(ExitCode.USAGE)
        }

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
        copyFromJar("init/chambers", chambersPath)
    }

    private fun initializeBin(binPath: Path) {
        copyFromJar("init/bin", binPath)
    }

    private fun copyFromJar(resource: String, destinationPath: Path) {
        val resourceUrl =
            Thread.currentThread().contextClassLoader.getResource(resource)
                ?: run {
                    println("Resource not found: $resource")
                    exitProcess(ExitCode.SOFTWARE)
                }

        try {
            FileSystems.newFileSystem(resourceUrl.toURI(), mapOf<String, Any>())
                .use { fs ->
                    val sourcePath = fs.getPath(resource)
                    Files.walkFileTree(
                        sourcePath,
                        object : SimpleFileVisitor<Path>() {
                            override fun preVisitDirectory(
                                dir: Path,
                                attrs: BasicFileAttributes
                            ): FileVisitResult {
                                val targetDir = destinationPath.resolve(
                                    sourcePath.relativize(dir).toString()
                                )
                                if (!Files.exists(targetDir)) {
                                    Files.createDirectory(targetDir)
                                }
                                return FileVisitResult.CONTINUE
                            }

                            override fun visitFile(
                                file: Path,
                                attrs: BasicFileAttributes
                            ): FileVisitResult {
                                val targetFile = destinationPath.resolve(
                                    sourcePath.relativize(file).toString()
                                )
                                Files.copy(
                                    file,
                                    targetFile,
                                    StandardCopyOption.REPLACE_EXISTING
                                )
                                return FileVisitResult.CONTINUE
                            }

                            override fun visitFileFailed(
                                file: Path,
                                exc: IOException
                            ): FileVisitResult {
                                println("Failed to copy file: $file due to ${exc.message}")
                                return FileVisitResult.CONTINUE
                            }
                        })
                }
        } catch (e: Exception) {
            println("Failed to copy resources: ${e.message}")
            exitProcess(ExitCode.SOFTWARE)
        }
    }

    fun setFilePermissionsTo755(path: Path) {
        @Suppress("SpellCheckingInspection") val permissions =
            PosixFilePermissions.fromString("rwxr-xr-x")
        Files.setPosixFilePermissions(path, permissions)
    }
}
