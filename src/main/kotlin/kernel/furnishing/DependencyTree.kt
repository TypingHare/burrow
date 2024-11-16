package burrow.kernel.furnishing

open class DependencyTree<T> {
    open class Node<T>(val element: T?) {
        val children = mutableListOf<Node<T>>()

        fun add(element: T) {
            children.add(Node(element))
        }
    }

    private class ResolveRoutine<T> {
        private val resolvedNodes = mutableSetOf<Node<T>>()

        fun resolve(node: Node<T>, consumer: (T) -> Unit) {
            if (resolvedNodes.add(node)) {
                node.children.forEach { resolve(it, consumer) }
                node.element?.let(consumer)
            }
        }
    }

    val root = Node<T>(null)

    fun resolve(resolver: (T) -> Unit) {
        ResolveRoutine<T>().resolve(root, resolver)
    }
}

class FurnishingDependencyTree : DependencyTree<Furnishing>()