package burrow.core.furniture.exception;

import org.jetbrains.annotations.NotNull;

public final class AmbiguousSimpleNameException extends Exception {
    public AmbiguousSimpleNameException(@NotNull final String simpleName) {
        super("Ambiguous furniture simple name: " + simpleName);
    }
}
