package burrow.kernel.furniture

import burrow.common.event.Event
import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.path.Persistable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.reflect.KClass

class Renovator(
    chamber: Chamber,
    val furnishingIds: MutableSet<String> = mutableSetOf()
) : ChamberModule(chamber), Persistable {
    /**
     * The path to the file storing a list of furnishing IDs.
     */
    private val path = chamber.getPath().resolve(FILE_NAME)

    val furnishings = mutableMapOf<String, Furnishing>()
    val depTree = DepTree<Furnishing>()

    override fun getPath(): Path = path

    @Throws(IOException::class)
    override fun save() {
        val type = object : TypeToken<List<String>>() {}.type
        val content = Gson().toJson(furnishingIds, type)
        Files.write(path, content.toByteArray())
    }

    @Throws(FurnishingsFileNotFoundException::class, IOException::class)
    override fun load() {
        if (!path.exists()) {
            throw FurnishingsFileNotFoundException()
        }

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
        depTree.resolveUniquely {
            it.modifyConfig(config)
            chamber.courier.post(FurnishingPostModifyConfigEvent(it))
        }
        depTree.resolveUniquely {
            it.assemble()
            chamber.courier.post(FurnishingPostAssembleEvent(it))
        }
        depTree.resolveUniquely {
            it.launch()
            chamber.courier.post(FurnishingPostLaunchEvent(it))
        }
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

    private fun getAllFurnishingIds(): Set<String> =
        furnishings.values.map { it.javaClass.name }.toSet()

    private fun getFurnishingIds(
        furnishingName: String,
        furnishingIds: Set<String>
    ): List<String> {
        return if (furnishingName.contains('.')) {
            when (furnishingIds.contains(furnishingName)) {
                true -> listOf(furnishingName)
                false -> emptyList()
            }
        } else {
            val name = furnishingName.lowercase()
            furnishingIds.filter {
                it.endsWith(".$name", ignoreCase = true)
            }
        }
    }

    /**
     * Gets furnishing IDs by a furnishing name.
     *
     * The furnishing name can be a full name (furnishing ID) or a simple name.
     */
    fun getFurnishingIds(furnishingName: String): List<String> =
        getFurnishingIds(furnishingName, getAllFurnishingIds())

    @Throws(
        FurnishingNameNotRecognizedException::class,
        MultipleFurnishingMatchedException::class
    )
    fun getUniqueFurnishingId(furnishingName: String): String {
        val furnishingIds = getFurnishingIds(furnishingName)
        return when (furnishingIds.size) {
            0 -> throw FurnishingNameNotRecognizedException(furnishingName)
            1 -> furnishingIds[0]
            else -> throw MultipleFurnishingMatchedException(furnishingIds)
        }
    }

    /**
     * Retrieves all furnishing classes available for this chamber.
     */
    fun getAvailableFurnishingClasses(): Set<FurnishingClass> {
        val isRoot = chamber.name == ChamberShepherd.ROOT_CHAMBER_NAME
        return burrow.warehouse.furnishingClasses
            .filter { isRoot || extractType(it) != Furniture.Type.ROOT }
            .toSet()
    }

    private fun getAvailableFurnishingIds(furnishingName: String): List<String> {
        return getFurnishingIds(
            furnishingName,
            getAvailableFurnishingClasses().map { extractId(it) }.toSet()
        )
    }

    @Throws(
        FurnishingNameNotRecognizedException::class,
        MultipleFurnishingMatchedException::class
    )
    fun getUniqueAvailableFurnishingId(furnishingName: String): String {
        val furnishingIds = getAvailableFurnishingIds(furnishingName)
        return when (furnishingIds.size) {
            0 -> throw FurnishingNameNotRecognizedException(furnishingName)
            1 -> furnishingIds[0]
            else -> throw MultipleFurnishingMatchedException(furnishingIds)
        }
    }

    fun getFurnishing(id: String) = furnishings[id]

    @Throws(
        IOException::class,
        CircularDependencyException::class,
        FurnishingNotFoundException::class,
        CreateFurnishingException::class,
        UnexpectedVersionException::class
    )
    private fun buildFurnishingTree() {
        val furnishingClasses = furnishingIds.map { findFurnishingClass(it) }
        resolveDependencies(
            emptyList(),
            furnishingClasses,
            depTree.root,
            mutableSetOf()
        )

        val config = chamber.config
        depTree.resolveUniquely {
            it.prepareConfig(config)
            chamber.courier.post(FurnishingPostPrepareConfigEvent(it))
        }
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
        node: DepTree.Node<Furnishing>,
        resolvedFurnishingIds: MutableSet<String>
    ) {
        deps.forEach { dep ->
            if (dep in path) {
                throw CircularDependencyException(path + dep)
            }

            val furnishing = when (dep.java.name in resolvedFurnishingIds) {
                true -> furnishings[dep.java.name]!!
                false -> createFurnishingInstance(dep)
            }
            val dependencies = furnishing.getDependencies()
            val dependencyClasses = dependencies.map {
                checkVersion(it.target, it.version)
                it.target
            }

            DepTree.Node(furnishing).apply {
                resolveDependencies(
                    path + dep,
                    dependencyClasses,
                    this,
                    resolvedFurnishingIds
                )
                node.children.add(this)
            }

            furnishings[extractId(furnishing::class)] = furnishing
            resolvedFurnishingIds.add(furnishing.javaClass.name)
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
        const val FILE_NAME = "furnishings.json"
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

class FurnishingNameNotRecognizedException(furnishingName: String) :
    RuntimeException("Furnishing name not recognized: $furnishingName")

class MultipleFurnishingMatchedException(furnishingIds: List<String>) :
    RuntimeException(buildString {
        appendLine("Multiple Furnishing IDs matched:")
        for (furnishingId in furnishingIds) {
            appendLine("  - $furnishingId")
        }
    })

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

class FurnishingPostPrepareConfigEvent(val furnishing: Furnishing) : Event()
class FurnishingPostModifyConfigEvent(val furnishing: Furnishing) : Event()
class FurnishingPostAssembleEvent(val furnishing: Furnishing) : Event()
class FurnishingPostLaunchEvent(val furnishing: Furnishing) : Event()
