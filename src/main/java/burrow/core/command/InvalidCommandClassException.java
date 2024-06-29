package burrow.core.command;

public class InvalidCommandClassException extends RuntimeException {
    public InvalidCommandClassException(final String commandClassName) {
        super("Fail to register command, as it is not annotated by picocli.CommandLine.Command: " +
            commandClassName);
    }
}
