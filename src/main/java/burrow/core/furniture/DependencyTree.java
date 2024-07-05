package burrow.core.furniture;

import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class DependencyTree {
    private final Node root = new Node(null);

    @NonNull
    public Node getRoot() {
        return root;
    }

    public void resolve(@NonNull final Consumer<Furniture> consumer) {
        new ResolveRoutine().resolveNode(root, consumer);
    }

    @NonNull
    public Node createNode(@NonNull final Furniture furniture) {
        return new Node(furniture);
    }

    public static final class Node {
        private final List<Node> children = new ArrayList<>();
        private final Furniture furniture;

        public Node(Furniture furniture) {this.furniture = furniture;}

        public void add(@NonNull final Node child) {
            children.add(child);
        }
    }

    private static final class ResolveRoutine {
        private final Set<Node> resolvedNode = new HashSet<>();

        private ResolveRoutine() {
            resolvedNode.add(null);
        }

        private void resolveNode(
            @NonNull final Node node,
            @NonNull final Consumer<Furniture> consumer
        ) {
            if (resolvedNode.contains(node)) {
                return;
            }

            for (final var child : node.children) {
                resolveNode(child, consumer);
            }

            if (node.furniture != null) {
                consumer.accept(node.furniture);
            }

            resolvedNode.add(node);
        }
    }
}
