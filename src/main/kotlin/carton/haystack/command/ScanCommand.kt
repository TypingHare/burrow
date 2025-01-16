package burrow.carton.haystack.command

import burrow.carton.haystack.Haystack
import burrow.carton.haystack.MultipleAbsolutePathsException
import burrow.carton.hoard.Hoard
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import burrow.kernel.terminal.ExitCode
import java.io.File

@BurrowCommand(
    name = "scan",
    header = ["Adds all files and directories within the path scope."],
    description = [
        "Scans directories specified by the path list. Files and directories " +
                "in the top hierarchy will be added. Hidden files and " +
                "directories started with '.' will not be added."
    ]
)
class ScanCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val haystack = use(Haystack::class)
        val paths = haystack.getPathList()
        val names = mutableSetOf<String>()
        use(Hoard::class).storage
            .getAllEntries()
            .map { names.add(it[Haystack.EntryKey.NAME]!!) }

        paths.forEach { scanDirectory(names, it) }

        return ExitCode.OK
    }

    private fun scanDirectory(names: MutableSet<String>, path: String) {
        val haystack = use(Haystack::class)
        val fileObject = File(path)
        if (!fileObject.isDirectory()) {
            return
        }

        val files = File(path).listFiles()?.toList() ?: emptyList()
        files.forEach { file ->
            val relativePath = file.name
            if (relativePath in names) return@forEach
            if (relativePath.startsWith(".")) return@forEach

            try {
                haystack.createEntry(relativePath)
                names.add(relativePath)
                stdout.println("Added: $relativePath -> ${file.absolutePath}")
            } catch (ex: MultipleAbsolutePathsException) {
                stderr.println("Failed to add $relativePath, as multiple absolute paths are found.")
                ex.candidateAbsolutePaths.forEach { stderr.println(it) }
            }
        }
    }
}