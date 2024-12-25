package burrow.kernel.furnishing

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import burrow.kernel.furnishing.annotation.Furniture
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.nio.file.Files
import kotlin.Throws
import kotlin.reflect.KClass

class Renovator(chamber: Chamber) : ChamberModule(chamber) {
    private val furnishingsFile =
        chamber.rootDirPath.resolve(FURNISHINGS_FILE_NAME).toFile()

    private val furnishings = mutableMapOf<String, Furnishing>()
    private val depTree = FurnishingDepTree()

    @Suppress("UNCHECKED_CAST")
    @Throws(FurnishingNotFoundException::class)
    private fun <T : Furnishing> getFurnishing(id: String): T {
        return furnishings[id] as T? ?: throw FurnishingNotFoundException(id)
    }

    @Throws(FurnishingNotFoundException::class)
    fun <T : Furnishing> getFurnishing(furnishingClass: KClass<T>): T {
        return getFurnishing(furnishingClass.java.name)
    }

    @Throws(FurnishingsFileNotFoundException::class)
    fun loadFromFile() {
        if (!furnishingsFile.exists()) {
            throw FurnishingsFileNotFoundException()
        }

        loadFromFile(loadFurnishingIds())
    }

    /**
     * Builds the furnishing dependency tree based on the furnishing IDs.
     */
    @Throws(
        IOException::class,
        CircularDependencyException::class,
        FurnishingNotFoundException::class,
        CreateFurnishingException::class,
        UnexpectedVersionException::class
    )
    private fun loadFromFile(furnishingIds: List<String>) {
        val furnishingClasses = furnishingIds.map {
            findFurnishingClass(it)
        }
        resolveDependencies(emptyList(), furnishingClasses, depTree.root)

        val config = chamber.config
        depTree.resolveUniquely { it.prepareConfig(config) }
    }

    /**
     * Loads a list of furnishing IDs from the furnishings file.
     */
    @Throws(IOException::class)
    private fun loadFurnishingIds(): List<String> {
        val content = Files.readString(furnishingsFile.toPath())
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(content, type)
    }

    /**
     * Save a list of furnishing IDs to the furnishings file.
     */
    @Throws(IOException::class)
    fun saveFurnishingIds(furnishingIds: List<String>) {
        val type = object : TypeToken<List<String>>() {}.type
        val content = Gson().toJson(furnishingIds, type)
        Files.write(furnishingsFile.toPath(), content.toByteArray())
    }

    /**
     * Initializes all furnishings by resolving the dependency tree.
     */
    fun initializeFurnishings() {
        val config = chamber.config
        depTree.resolveUniquely { it.modifyConfig(config) }
        depTree.resolveUniquely { it.assemble() }
        depTree.resolveUniquely { it.launch() }
    }

    @Throws(
        CircularDependencyException::class,
        FurnishingNotFoundException::class,
        CreateFurnishingException::class,
        UnexpectedVersionException::class
    )
    private fun resolveDependencies(
        path: List<FurnishingClass>,
        deps: List<FurnishingClass>,
        node: DepTree.Node<Furnishing>
    ) {
        deps.forEach { dep ->
            if (dep in path) {
                throw CircularDependencyException(path + dep)
            }

            val furnishing = createFurnishingInstance(dep)
            val dependencies = furnishing.getDependencies()
            val dependencyClasses = dependencies.map {
                checkVersion(it.target, it.version)
                it.target
            }

            DepTree.Node(furnishing).apply {
                resolveDependencies(path + dep, dependencyClasses, this)
                node.children.add(this)
            }

            furnishings[getId(furnishing)] = furnishing
        }
    }

    private fun checkVersion(
        furnishingClass: FurnishingClass,
        expectedVersion: String
    ) {
        val furniture =
            furnishingClass.java.getAnnotation(Furniture::class.java)!!
        val actualVersion = furniture.version

        if (actualVersion != expectedVersion) {
            throw UnexpectedVersionException(
                furnishingClass,
                actualVersion,
                expectedVersion
            )
        }
    }

    @Throws(FurnishingNotFoundException::class)
    private fun findFurnishingClass(id: String): FurnishingClass =
        burrow.warehouse.getFurnishingClass(id)
            ?: throw FurnishingNotFoundException(id)

    @Throws(CreateFurnishingException::class)
    private fun createFurnishingInstance(furnishingClass: FurnishingClass): Furnishing {
        try {
            return furnishingClass.java
                .getDeclaredConstructor(Renovator::class.java)
                .apply { isAccessible = true }
                .newInstance(this)
        } catch (ex: Exception) {
            throw CreateFurnishingException(furnishingClass.java.name, ex)
        }
    }

    companion object {
        const val FURNISHINGS_FILE_NAME = "furnishings.json"
    }
}

class FurnishingNotFoundException(furnishingId: String) :
    RuntimeException(furnishingId)

class FurnishingsFileNotFoundException :
    RuntimeException("Furnishings file not found.")

class CircularDependencyException(depPath: List<FurnishingClass>) :
    RuntimeException(
        "Circular dependency found: \n" +
                depPath.joinToString(" -> ", transform = { it.java.name })
    )

class CreateFurnishingException(furnishingId: String, cause: Exception) :
    RuntimeException("Failed to create Furnishing: $furnishingId", cause)

class UnexpectedVersionException(
    furnishingClass: FurnishingClass,
    actualVersion: String,
    expectedString: String
) : RuntimeException(
    """
        Unexpected version of: ${furnishingClass.java.name}.
        actual version: ${actualVersion}; expected version: $expectedString
    """.trimIndent()
)