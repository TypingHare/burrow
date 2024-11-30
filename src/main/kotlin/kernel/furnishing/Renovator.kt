package burrow.kernel.furnishing

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
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
    private fun loadFromFile(furnishingIds: List<String>) {
        resolveDependencies(emptyList(), furnishingIds, depTree.root)

        val config = chamber.config
        depTree.resolveUniquely { it.prepareConfig(config) }
    }

    /**
     * Loads a list of furnishing IDs from the furnishings file.
     */
    private fun loadFurnishingIds(): List<String> {
        val content = Files.readString(furnishingsFile.toPath())
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(content, type)
    }

    /**
     * Save a list of furnishing IDs to the furnishings file.
     */
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
        CreateFurnishingException::class
    )
    private fun resolveDependencies(
        path: List<String>,
        deps: List<String>,
        node: DepTree.Node<Furnishing>
    ) {
        deps.forEach { dep ->
            if (dep in path) {
                throw CircularDependencyException(dep)
            }

            val furnishing = loadById(dep)
            val nextNode = DepTree.Node(furnishing)
            resolveDependencies(
                path + dep,
                furnishing.getDependencies(),
                nextNode
            )
            node.children.add(nextNode)

            registerFurnishing(furnishing)
        }
    }

    private fun registerFurnishing(furnishing: Furnishing) {
        furnishings.putIfAbsent(furnishing.javaClass.name, furnishing)
    }

    @Throws(
        FurnishingNotFoundException::class,
        CreateFurnishingException::class
    )
    private fun loadById(id: String): Furnishing {
        return createFurnishingInstance(findFurnishingClass(id))
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

class CircularDependencyException(furnishingId: String) :
    RuntimeException("Circular dependency found: $furnishingId")

class CreateFurnishingException(furnishingId: String, cause: Exception) :
    RuntimeException("Failed to create Furnishing: $furnishingId", cause)