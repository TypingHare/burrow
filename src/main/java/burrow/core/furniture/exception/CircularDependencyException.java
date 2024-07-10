package burrow.core.furniture.exception;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CircularDependencyException extends RuntimeException {
    public CircularDependencyException(@NotNull final List<String> dependencyPath) {
        super("Circular dependency found: \n" + String.join(" -> \n", dependencyPath));
    }
}
