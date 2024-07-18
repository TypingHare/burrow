package burrow.core.chamber;

import org.jetbrains.annotations.NotNull;

public final class ChamberInitializationException extends Exception {
    public ChamberInitializationException(
        @NotNull final String chamberName,
        @NotNull final Throwable cause
    ) {
        super("Fail to initialize chamber: <" + chamberName + ">", cause);
    }
}
