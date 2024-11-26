package burrow.kernel.furnishing

open class DepTree<T> {
    class Node<T>(val element: T?) {
        val children = mutableListOf<Node<T>>()

        fun add(element: T) {
            children.add(Node(element))
        }
    }

    protected class ResolveRoutine<T> {
        fun resolve(node: Node<T>, resolver: (T) -> Unit) {
            node.children.forEach { resolve(it, resolver) }
            node.element?.let(resolver)
        }

        fun resolveWithoutDuplicates(
            node: Node<T>,
            resolver: (T) -> Unit,
            resolvedNodes: MutableSet<Node<T>>
        ) {
            if (resolvedNodes.add(node)) {
                node.children.forEach {
                    resolveWithoutDuplicates(it, resolver, resolvedNodes)
                }
                node.element?.let(resolver)
            }
        }
    }

    val root = Node<T>(null)

    fun resolve(resolver: (T) -> Unit) {
        ResolveRoutine<T>().resolve(root, resolver)
    }

    fun resolveWithoutDuplicates(resolver: (T) -> Unit) {
        ResolveRoutine<T>().resolveWithoutDuplicates(
            root,
            resolver,
            mutableSetOf()
        )
    }
}

class FurnishingDepTree : DepTree<Furnishing>()