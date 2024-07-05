package burrow.core.furniture;

import org.springframework.lang.NonNull;

import java.util.List;

public final class CircularDependencyException extends RuntimeException {
    public CircularDependencyException(@NonNull final List<String> dependencyPath) {
        super("Circular dependency found: \n" + String.join(" -> \n", dependencyPath));
    }
}
