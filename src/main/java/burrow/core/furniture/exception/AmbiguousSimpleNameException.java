package burrow.core.furniture.exception;

import org.springframework.lang.NonNull;

public final class AmbiguousSimpleNameException extends Exception {
    public AmbiguousSimpleNameException(@NonNull final String simpleName) {
        super("Ambiguous furniture simple name: " + simpleName);
    }
}
