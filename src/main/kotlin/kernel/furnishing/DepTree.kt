package burrow.kernel.furnishing

open class DepTree<T> {
    val root = Node<T>(null)

    fun resolve(action: (T) -> Unit) {
        DepResolver<T>().resolveDependencies(root, action)
    }

    fun resolveUniquely(action: (T) -> Unit) {
        DepResolver<T>().resolveUniqueDependencies(
            root,
            action,
            mutableSetOf()
        )
    }

    class Node<T>(val element: T?) {
        val children = mutableListOf<Node<T>>()

        fun addChild(element: T) {
            children.add(Node(element))
        }
    }

    protected class DepResolver<T> {
        fun resolveDependencies(node: Node<T>, action: (T) -> Unit) {
            node.children.forEach { resolveDependencies(it, action) }
            node.element?.let(action)
        }

        fun resolveUniqueDependencies(
            node: Node<T>,
            action: (T) -> Unit,
            visitedNodes: MutableSet<Node<T>>
        ) {
            if (visitedNodes.add(node)) {
                node.children.forEach {
                    resolveUniqueDependencies(it, action, visitedNodes)
                }
                node.element?.let(action)
            }
        }
    }
}

class FurnishingDepTree : DepTree<Furnishing>()
