package burrow.common.resource

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

fun copyResourceFromJar(resource: String, destinationPath: Path): Boolean {
    val resourceUrl =
        Thread.currentThread().contextClassLoader.getResource(resource)
            ?: run {
                println("Resource not found: $resource")
                return false
            }

    try {
        FileSystems.newFileSystem(resourceUrl.toURI(), mapOf<String, Any>())
            .use { fs ->
                val sourcePath = fs.getPath(resource)
                Files.walkFileTree(
                    sourcePath,
                    ResourceCopyFileVisitor(sourcePath, destinationPath)
                )
            }

        return true
    } catch (ex: Exception) {
        println("Failed to copy resources [$resource] to $destinationPath")
        ex.printStackTrace()

        return false
    }
}