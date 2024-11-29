package burrow.kernel.furnishing

import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.reflect.KClass

class Renovator(chamber: Chamber) : ChamberModule(chamber) {
    private val furnishingsFilePath: Path =
        chamber.rootPath.resolve(Burrow.Standard.FURNISHINGS_FILE_NAME)

    val furnishings = mutableMapOf<String, Furnishing>()
    val depTree = FurnishingDepTree()
    private val labelMap = mutableMapOf<String, MutableSet<String>>()

    private fun getFurnishing(id: String) = furnishings[id]

    @Suppress("UNCHECKED_CAST")
    @Throws(FurnishingNotFoundException::class)
    fun <T : Furnishing> getFurnishing(furnishingClass: KClass<T>): T? =
        getFurnishing(furnishingClass.java.name) as T?

    fun loadFurnishings() {
        if (!furnishingsFilePath.exists()) {
            throw FurnishingsFileNotFoundException(furnishingsFilePath)
        }

        loadFurnishings(loadFurnishingIds())
    }

    private fun loadFurnishings(furnishingIds: List<String>) {
        resolveDependencies(emptyList(), furnishingIds, depTree.root)

        val config = chamber.config
        depTree.resolveWithoutDuplicates { it.prepareConfig(config) }
    }

    private fun loadFurnishingIds(): List<String> {
        val content = Files.readString(furnishingsFilePath)
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(content, type)
    }

    fun saveFurnishingIds(furnishingIds: Set<String>) {
        val type = object : TypeToken<List<String>>() {}.type
        val content = Gson().toJson(furnishingIds, type)
        Files.write(furnishingsFilePath, content.toByteArray())
    }

    fun initializeFurnishings() {
        val config = chamber.config
        depTree.resolveWithoutDuplicates { it.modifyConfig(config) }
        depTree.resolveWithoutDuplicates { it.assemble() }
        depTree.resolveWithoutDuplicates { it.launch() }
    }

    private fun resolveDependencies(
        path: List<String>,
        deps: List<String>,
        node: DepTree.Node<Furnishing>
    ) {
        deps.forEach { dep ->
            require(dep !in path) {
                "Circular dependency detected for $dep"
            }

            furnishings[dep] ?: run {
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
    }

    private fun registerFurnishing(furnishing: Furnishing) {
        val id = furnishing.javaClass.name
        if (id !in furnishings) {
            furnishings[id] = furnishing
            val label = furnishing.getLabel()
            labelMap.computeIfAbsent(label) { mutableSetOf() }.add(id)
        }
    }

    private fun loadById(id: String): Furnishing {
        val clazz =
            findFurnishingClass(id) ?: throw FurnishingNotFoundException(id)
        return createFurnishingInstance(clazz)
    }

    private fun findFurnishingClass(id: String): FurnishingClass? {
        val furnishingClass = burrow.furnishingWarehouse.getFurnishingClass(id)
        if (furnishingClass != null) {
            return furnishingClass
        }

        val sp = id.split(".")
        val classSimpleName =
            sp[sp.size - 1].replaceFirstChar { it.uppercase() }
        val otherId = sp.joinToString(".") + "." + classSimpleName
        return burrow.furnishingWarehouse.getFurnishingClass(otherId)
    }

    private fun createFurnishingInstance(clazz: FurnishingClass): Furnishing {
        val constructor = clazz.java.getDeclaredConstructor(Chamber::class.java)
            .apply { isAccessible = true }
        return constructor.newInstance(chamber)
    }
}

class FurnishingNotFoundException(id: String) :
    RuntimeException("Furnishing not found: $id")

class FurnishingsFileNotFoundException(private val path: Path) :
    RuntimeException("Furnishings file not found: $path") {
    fun getPath(): Path = path
}