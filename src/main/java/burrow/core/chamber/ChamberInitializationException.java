package burrow.core.chamber;

public class ChamberInitializationException extends Exception {
    public ChamberInitializationException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
