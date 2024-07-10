package burrow.core.command;

import burrow.core.chamber.ChamberModule;
import burrow.core.common.ErrorUtility;
import burrow.core.furniture.Furniture;
import burrow.core.furniture.exception.FurnitureNotFoundException;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.Collection;
import java.util.List;
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
    public Command(@NotNull final CommandContext commandContext) {
        super(commandContext.getChamberContext().getChamber());
        this.commandContext = commandContext;
        this.buffer = commandContext.getBuffer();
    }

    @NotNull
    public static CommandLine.Command getCommandAnnotation(
        @NotNull final Class<? extends Command> commandClass) {
        final var commandAnnotation = commandClass.getAnnotation(CommandLine.Command.class);
        if (commandAnnotation == null) {
            throw new InvalidCommandClassException(commandClass.getName());
        }

        return commandAnnotation;
    }

    @NotNull
    public static String getName(@NotNull final Class<? extends Command> commandClass) {
        return getCommandAnnotation(commandClass).name();
    }

    @NotNull
    public static String[] getDescription(@NotNull final Class<? extends Command> commandClass) {
        return getCommandAnnotation(commandClass).description();
    }

    @NotNull
    public static String getType(@NotNull final Class<? extends Command> commandClass) {
        final var commandType = commandClass.getAnnotation(CommandType.class);
        return commandType == null ? CommandType.OTHER : commandType.value();
    }

    @NotNull
    public <T extends Furniture> T use(@NotNull final Class<T> furnitureClass) {
        final T furniture = getRenovator().getFurniture(furnitureClass.getName());
        if (furniture == null) {
            throw new FurnitureNotFoundException(furnitureClass.getName());
        }

        return furniture;
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
    public void bufferAppendLines(@NotNull final Collection<String> lines) {
        if (!lines.isEmpty()) {
            buffer.append(String.join("\n", lines));
        }
    }

    public void bufferAppendThrowable(@NotNull final Throwable throwable) {
        bufferAppendLines(ErrorUtility.getCauseStack(throwable));
    }

    @NotNull
    public Integer dispatch(
        @NotNull final Class<? extends Command> commandClass,
        @NotNull final List<String> commandArgs
    ) {
        final String commandName = getName(commandClass);
        commandContext.setCommandName(commandName);
        commandContext.setCommandArgs(commandArgs);

        getChamber().getExecuteCommandChain().apply(commandContext);

        return commandContext.getExitCode();
    }
}
