package burrow.kernel.furnishing

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberModule
import kotlin.reflect.KClass

class Renovator(chamber: Chamber) : ChamberModule(chamber) {
    val furnishings = mutableMapOf<String, Furnishing>()
    val depTree = FurnishingDepTree()
    private val labelMap = mutableMapOf<String, MutableSet<String>>()

    private fun getFurnishing(id: String) = furnishings[id]

    @Suppress("UNCHECKED_CAST")
    fun <T : Furnishing> getFurnishing(furnishingClass: KClass<T>): T? =
        getFurnishing(furnishingClass.java.name) as T?

    fun loadFurnishings(furnishingIds: List<String>) {
        resolveDependencies(emptyList(), furnishingIds, depTree.root)

        depTree.resolveWithoutRepetition { it.prepareConfig(config) }
    }

    fun initializeFurnishings() {
        depTree.resolveWithoutRepetition { it.modifyConfig(config) }
        depTree.resolveWithoutRepetition { it.assemble() }
        depTree.resolveWithoutRepetition { it.launch() }
    }

    private fun resolveDependencies(
        path: List<String>,
        deps: List<String>,
        node: DepTree.Node<Furnishing>
    ) {
        deps.forEach { dep ->
            require(dep !in path) {
                "Circular dep detected for $dep"
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