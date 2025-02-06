package burrow.common.resource

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class ResourceCopyFileVisitor(
    private val sourcePath: Path,
    private val destinationPath: Path
) : SimpleFileVisitor<Path>() {
    private fun getTargetPath(path: Path): Path =
        destinationPath.resolve(sourcePath.relativize(path).toString())

    override fun preVisitDirectory(
        dir: Path,
        attrs: BasicFileAttributes
    ): FileVisitResult {
        val targetDir = getTargetPath(dir)
        if (!Files.exists(targetDir)) {
            Files.createDirectory(targetDir)
        }

        return FileVisitResult.CONTINUE
    }

    override fun visitFile(
        file: Path,
        attrs: BasicFileAttributes
    ): FileVisitResult {
        Files.copy(
            file,
            getTargetPath(file),
            StandardCopyOption.REPLACE_EXISTING
        )

        return FileVisitResult.CONTINUE
    }

    override fun visitFileFailed(file: Path, ex: IOException): FileVisitResult {
        println("Failed to copy file: $file")
        ex.printStackTrace()

        return FileVisitResult.CONTINUE
    }
}