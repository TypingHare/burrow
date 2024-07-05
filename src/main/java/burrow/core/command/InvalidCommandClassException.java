package burrow.core.command;

import org.springframework.lang.NonNull;

public final class InvalidCommandClassException extends RuntimeException {
    public InvalidCommandClassException(@NonNull final String commandClassName) {
        super("Fail to register command, as it is not annotated by picocli.CommandLine.Command: " +
            commandClassName);
    }
}
