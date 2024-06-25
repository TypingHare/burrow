package burrow.core.furniture;

public class AmbiguousSimpleNameException extends Exception {
    public AmbiguousSimpleNameException(final String simpleName) {
        super("Ambiguous furniture simple name: " + simpleName);
    }
}
