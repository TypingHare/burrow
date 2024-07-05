package burrow.core.command;

import burrow.core.chamber.ChamberContext;
import burrow.core.chamber.ChamberModule;
import burrow.core.common.ErrorUtility;
import burrow.core.furniture.Furniture;
import burrow.core.furniture.FurnitureNotFoundException;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.util.Collection;
import java.util.concurrent.Callable;

public abstract class Command extends ChamberModule implements Callable<Integer>,
    CommandLine.IParameterExceptionHandler,
    CommandLine.IExecutionExceptionHandler {
    /**
     * The CommandContext associated with this Command
     */
    protected final CommandContext commandContext;

    /**
     * StringBuilder instance for buffering output or other data within the context.
     */
    protected final StringBuilder buffer;

    /**
     * Constructs a new Command with the given RequestContext. Initializes the buffer from the
     * context.
     * @param commandContext the RequestContext in which this command operates
     */
    public Command(@NonNull final CommandContext commandContext) {
        super(ChamberContext.Hook.chamber.getNonNull(CommandContext.Hook.chamberContext.getNonNull(commandContext)));
        this.commandContext = commandContext;
        this.buffer = CommandContext.Hook.buffer.get(commandContext);
    }

    @NonNull
    public <T extends Furniture> T use(@NonNull final Class<T> furnitureClass) {
        final T furniture = getRenovator().getFurniture(furnitureClass.getName());
        if (furniture == null) {
            throw new FurnitureNotFoundException(furnitureClass.getName());
        }

        return furniture;
    }

    @NonNull
    public static CommandLine.Command getCommandAnnotation(
        @NonNull final Class<? extends Command> commandClass) {
        final var commandAnnotation = commandClass.getAnnotation(CommandLine.Command.class);
        if (commandAnnotation == null) {
            throw new InvalidCommandClassException(commandClass.getName());
        }

        return commandAnnotation;
    }

    @NonNull
    public static String getName(@NonNull final Class<? extends Command> commandClass) {
        return getCommandAnnotation(commandClass).name();
    }

    @NonNull
    public static String[] getDescription(@NonNull final Class<? extends Command> commandClass) {
        return getCommandAnnotation(commandClass).description();
    }

    @NonNull
    public static String getType(@NonNull final Class<? extends Command> commandClass) {
        final var commandType = commandClass.getAnnotation(CommandType.class);
        return commandType == null ? CommandType.OTHER : commandType.value();
    }

    /**
     * Handles exceptions thrown during command line parameter parsing.
     * @param ex   the exception thrown during parsing
     * @param args the arguments that were passed to the command
     * @return an exit code indicating the result of the error handling
     */
    @Override
    public int handleParseException(final CommandLine.ParameterException ex, final String[] args) {
        if (ex instanceof CommandLine.MissingParameterException) {
            final var missingParams = ((CommandLine.MissingParameterException) ex).getMissing();
            final var paramLabelList =
                missingParams.stream().map(CommandLine.Model.ArgSpec::paramLabel).toList();
            buffer.append("Missing parameters: ").append(String.join(" ", paramLabelList));
        } else {
            buffer.append("Fail to parse command: ").append(ex.getMessage());
        }

        return CommandLine.ExitCode.SOFTWARE;
    }

    /**
     * Handles exceptions thrown during command execution.
     * @param ex              the exception thrown during execution
     * @param commandLine     the command line that was being executed
     * @param fullParseResult the result of parsing the command line arguments
     * @return an exit code indicating the result of the error handling
     */
    @Override
    public int handleExecutionException(
        final Exception ex,
        final CommandLine commandLine,
        final CommandLine.ParseResult fullParseResult) {
        bufferAppendThrowable(ex);

        return CommandLine.ExitCode.SOFTWARE;
    }

    /**
     * Appends a collection of lines to the buffer, joining them with newline characters.
     * @param lines the lines to append to the buffer
     */
    public void bufferAppendLines(@NonNull final Collection<String> lines) {
        if (!lines.isEmpty()) {
            buffer.append(String.join("\n", lines));
        }
    }

    public void bufferAppendThrowable(@NonNull final Throwable throwable) {
        bufferAppendLines(ErrorUtility.getCauseStack(throwable));
    }
}
