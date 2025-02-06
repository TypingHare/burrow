package burrow.carton.clutter.command

import burrow.carton.clutter.InvalidCartonNameException
import burrow.common.resource.copyResourceFromJar
import burrow.kernel.terminal.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories

@BurrowCommand(
    name = "carton.new",
    header = ["Creates a new carton."]
)
class CartonNewCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = ["Carton's name; allows letters, numbers, and underscores."]
    )
    private var cartonName = ""

    override fun call(): Int {
        if (!cartonName.matches(Regex("[A-Za-z0-9_]+"))) {
            throw InvalidCartonNameException(cartonName)
        }

        val cartonDirPath = getWorkingDirectory().resolve("$cartonName.carton")
        cartonDirPath.createDirectories()

        stdout.println("Carton directory: $cartonDirPath")
        if (!copyResourceFromJar("carton-template", cartonDirPath)) {
            stderr.println("Failed to copy carton template.")
            return ExitCode.SOFTWARE
        }

        stdout.println("Created: $cartonName.carton")
        processTemplate(
            cartonDirPath.resolve("build.gradle.kts"), mapOf(
                "carton_name" to cartonName,
            )
        )

        val furnishingClassName = cartonNameToFurnishingClassName(cartonName)
        val furnishingClassFilePath =
            cartonDirPath.resolve("src/main/kotlin/carton/name/Name.kt")
        processTemplate(
            furnishingClassFilePath,
            mapOf(
                "carton_name" to cartonName,
                "furnishing_class_name" to furnishingClassName
            )
        )

        val listCommandFilePath =
            cartonDirPath.resolve("src/main/kotlin/carton/name/command/ListCommand.kt")
        processTemplate(listCommandFilePath, mapOf("carton_name" to cartonName))

        // Rename files
        rename(furnishingClassFilePath, "$furnishingClassName.kt")
        rename(
            cartonDirPath.resolve("src/main/kotlin/carton/name"),
            cartonNameToPackageName(cartonName)
        )

        return ExitCode.OK
    }

    private fun cartonNameToFurnishingClassName(cartonName: String): String {
        return cartonName
            .split("-")
            .joinToString("") {
                it.replaceFirstChar { char -> char.uppercase() }
            }
    }

    private fun cartonNameToPackageName(cartonName: String): String {
        return cartonName.replace("-", "")
    }

    /**
     * Processes a template file by replacing placeholder variables with their
     * values.
     *
     * Each placeholder in the template file should be in the format
     * `{{variableName}}`.The function will replace all occurrences of these
     * placeholders with their corresponding values from the provided mapping.
     *
     * @param templatePath Path to the template file to be processed
     * @param replacements Map of variable names to their replacement values
     * @throws IllegalStateException if the file cannot be read or written
     */
    private fun processTemplate(
        templatePath: Path,
        replacements: Map<String, String>
    ) {
        try {
            var content = Files.readString(templatePath)

            replacements.forEach { (variable, value) ->
                content = content.replace("{{$variable}}", value)
            }

            Files.writeString(templatePath, content)
        } catch (ex: IOException) {
            throw IllegalStateException(
                "Failed to process template file: ${templatePath.fileName}",
                ex
            )
        }
    }

    /**
     * Renames a file or directory.
     *
     * @param source Path to the file or directory to rename
     * @param newName New name for the file or directory
     * @param replaceExisting If true, replaces any existing file with the same
     * name
     * @throws IllegalStateException if the rename operation fails
     * @throws IllegalArgumentException if the source doesn't exist
     */
    private fun rename(
        source: Path,
        newName: String,
        replaceExisting: Boolean = false
    ) {
        try {
            if (!Files.exists(source)) {
                throw IllegalArgumentException("Source does not exist: $source")
            }

            val target = source.resolveSibling(newName)

            if (replaceExisting) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING)
            } else {
                Files.move(source, target)
            }
        } catch (ex: IOException) {
            throw IllegalStateException(
                "Failed to rename ${source.fileName} to $newName",
                ex
            )
        }
    }
}

