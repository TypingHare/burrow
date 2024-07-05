package burrow.core.chamber;

public final class ChamberInitializationException extends Exception {
    public ChamberInitializationException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
