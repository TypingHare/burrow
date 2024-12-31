package burrow.kernel.furniture

open class DepTree<T> {
    val root = Node<T>(null)

    fun resolve(action: (T) -> Unit) {
        resolve(action, root)
    }

    fun resolve(action: (T) -> Unit, node: Node<T>) {
        node.children.forEach { resolve(action, it) }
        node.element?.let(action)
    }

    fun resolveUniquely(action: (T) -> Unit) {
        resolveUniquely(action, root, mutableSetOf())
    }

    fun resolveUniquely(
        action: (T) -> Unit,
        node: Node<T>,
        visitedNodes: MutableSet<Node<T>>
    ) {
        if (visitedNodes.add(node)) {
            node.children.forEach { resolveUniquely(action, it, visitedNodes) }
            node.element?.let(action)
        }
    }

    class Node<T>(val element: T?) {
        val children = mutableListOf<Node<T>>()
        fun addChild(element: T): Boolean = children.add(Node(element))
    }
}