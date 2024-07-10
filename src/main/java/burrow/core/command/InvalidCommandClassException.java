package burrow.core.command;

import org.jetbrains.annotations.NotNull;

public final class InvalidCommandClassException extends RuntimeException {
    public InvalidCommandClassException(@NotNull final String commandClassName) {
        super("Fail to register command, as it is not annotated by picocli.CommandLine.Command: " +
            commandClassName);
    }
}
