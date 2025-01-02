package burrow.kernel.furniture

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import burrow.kernel.path.Persistable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass

class Renovator(
    chamber: Chamber,
    private val furnishingIds: MutableSet<String> = mutableSetOf()
) : ChamberModule(chamber), Persistable {
    /**
     * The path to the file storing a list of furnishing IDs.
     */
    private val path = chamber.getPath().resolve(FURNISHINGS_FILE_NAME)

    val furnishings = mutableMapOf<String, Furnishing>()
    val depTree = DepTree<Furnishing>()

    override fun getPath(): Path = path

    @Throws(IOException::class)
    override fun save() {
        val type = object : TypeToken<List<String>>() {}.type
        val content = Gson().toJson(furnishingIds, type)
        Files.write(path, content.toByteArray())
    }

    @Throws(IOException::class)
    override fun load() {
        val content = Files.readString(path)
        val type = object : TypeToken<List<String>>() {}.type
        val furnishingIds: List<String> = Gson().fromJson(content, type)
        this.furnishingIds.clear()
        this.furnishingIds.addAll(furnishingIds)

        buildFurnishingTree()
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

    /**
     * Discards all furnishings by resolving the dependency tree.
     */
    fun discardFurnishings() {
        depTree.resolveUniquely { it.discard() }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(FurnishingNotFoundException::class)
    fun <T : Furnishing> getFurnishing(furnishingClass: KClass<T>): T? =
        getFurnishing(furnishingClass.java.name) as T?

    private fun getFurnishing(id: String) = furnishings[id]

    @Throws(
        IOException::class,
        CircularDependencyException::class,
        FurnishingNotFoundException::class,
        CreateFurnishingException::class,
        UnexpectedVersionException::class
    )
    private fun buildFurnishingTree() {
        val furnishingClasses = furnishingIds.map { findFurnishingClass(it) }
        resolveDependencies(emptyList(), furnishingClasses, depTree.root)

        val config = chamber.config
        depTree.resolveUniquely { it.prepareConfig(config) }
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

            furnishings[extractId(furnishing::class)] = furnishing
        }
    }

    private fun checkVersion(
        furnishingClass: FurnishingClass,
        expectedVersion: String
    ) {
        val furniture = extractFurniture(furnishingClass)!!
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
        actual version:   $actualVersion
        expected version: $expectedString
    """.trimIndent()
)