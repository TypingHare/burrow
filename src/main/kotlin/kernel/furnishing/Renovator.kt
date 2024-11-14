package burrow.kernel.furnishing

import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import kotlin.reflect.KClass

class Renovator(chamber: Chamber) : ChamberModule(chamber) {
    companion object {
        const val STANDARD_FURNISHING_ID =
            Burrow.Standard.CARTON_PACKAGE + ".standard.Standard"
    }

    val furnishings = mutableMapOf<String, Furnishing>()
    val dependencyTree = FurnishingDependencyTree()
    private val labelMap = mutableMapOf<String, MutableSet<String>>()

    private fun getFurnishing(id: String) = furnishings[id]

    @Suppress("UNCHECKED_CAST")
    fun <T : Furnishing> getFurnishing(furnishingClass: KClass<T>): T? =
        getFurnishing(furnishingClass.java.name) as T?

    fun loadFurnishings(furnishingIds: List<String>) {
        resolveDependencies(emptyList(), furnishingIds, dependencyTree.root)

        dependencyTree.resolve { it.prepareConfig(config) }
    }

    fun initializeFurnishings() {
        dependencyTree.resolve { it.modifyConfig(config) }
        dependencyTree.resolve { it.assemble() }
        dependencyTree.resolve { it.launch() }
    }

    private fun resolveDependencies(
        path: List<String>,
        dependencies: List<String>,
        node: FurnishingDependencyTree.Node
    ) {
        dependencies.forEach { dependency ->
            require(dependency !in path) {
                "Circular dependency detected for $dependency"
            }

            furnishings[dependency] ?: run {
                val furnishing = loadById(dependency)
                val nextNode = FurnishingDependencyTree.Node(furnishing)
                resolveDependencies(
                    path + dependency,
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

        val sp = id.split(".");
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

class FurnishingDependencyTree {
    class Node(val furnishing: Furnishing?) {
        val children = mutableListOf<Node>()
    }

    private class ResolveRoutine {
        private val resolvedNodes = mutableSetOf<Node>()

        fun resolve(node: Node, consumer: (Furnishing) -> Unit) {
            if (resolvedNodes.add(node)) {
                node.children.forEach { resolve(it, consumer) }
                node.furnishing?.let(consumer)
            }
        }
    }

    val root = Node(null)

    fun resolve(resolver: (Furnishing) -> Unit) {
        ResolveRoutine().resolve(root, resolver)
    }
}

class FurnishingNotFoundException(id: String) :
    RuntimeException("Furnishing not found: $id")