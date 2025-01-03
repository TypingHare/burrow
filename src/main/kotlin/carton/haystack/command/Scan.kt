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
    header = ["Adds all files and directories within the path scope."]
)
class Scan(data: CommandData) : Command(data) {
    override fun call(): Int {
        val haystack = use(Haystack::class)
        val paths = haystack.getPaths()
        val relativePaths = mutableSetOf<String>()
        use(Hoard::class).getAllEntries().map {
            relativePaths.add(it[Haystack.EntryKey.RELATIVE_PATH]!!)
        }

        for (path in paths) {
            val files = File(path).listFiles()?.toList() ?: emptyList()
            files.forEach { file ->
                val relativePath = file.name
                if (relativePath in relativePaths) return@forEach
                if (relativePath.startsWith(".")) return@forEach

                try {
                    haystack.createEntry(relativePath)
                    stdout.println("Added: $relativePath -> ${file.absolutePath}")
                } catch (ex: MultipleAbsolutePathsException) {
                    stderr.println("Failed to add relative path: $relativePath")
                    stderr.println("Multiple absolute paths found: ")
                    ex.candidateAbsolutePaths.forEach { stderr.println(it) }

                    return ExitCode.USAGE
                }
            }
        }

        return ExitCode.OK
    }
}