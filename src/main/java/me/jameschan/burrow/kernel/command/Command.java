package me.jameschan.burrow.kernel.command;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import me.jameschan.burrow.kernel.ChamberModule;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import me.jameschan.burrow.kernel.utility.ErrorUtility;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

/**
 * Abstract class representing a command that can be executed within a given request context. This
 * class implements Callable with a return type of Integer, allowing it to be executed by a thread
 * or executor.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class Command extends ChamberModule
    implements Callable<Integer>,
        CommandLine.IParameterExceptionHandler,
        CommandLine.IExecutionExceptionHandler {

  // The RequestContext associated with this Command
  protected final RequestContext requestContext;

  // StringBuilder instance for buffering output or other data within the context
  protected final StringBuilder buffer;

  /**
   * Constructs a new Command with the given RequestContext. Initializes the buffer from the
   * context.
   *
   * @param requestContext the RequestContext in which this command operates
   */
  public Command(final RequestContext requestContext) {
    super(requestContext.getChamberContext().getChamber());
    this.requestContext = requestContext;
    this.buffer = requestContext.getBuffer();
  }

  /**
   * Handles exceptions thrown during command line parameter parsing.
   *
   * @param ex the exception thrown during parsing
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

    return ExitCode.ERROR;
  }

  /**
   * Handles exceptions thrown during command execution.
   *
   * @param ex the exception thrown during execution
   * @param commandLine the command line that was being executed
   * @param fullParseResult the result of parsing the command line arguments
   * @return an exit code indicating the result of the error handling
   */
  @Override
  public int handleExecutionException(
      final Exception ex,
      final CommandLine commandLine,
      final CommandLine.ParseResult fullParseResult) {
    bufferAppendThrowable(ex);

    return ExitCode.ERROR;
  }

  /**
   * Retrieves an instance of Furniture by its class type using the renovator within the context.
   *
   * @param furnitureClass the class type of the Furniture to retrieve
   * @param <T> the type of the Furniture
   * @return an instance of the specified Furniture class
   */
  @NonNull
  public <T extends Furniture> T getFurniture(final Class<T> furnitureClass) {
    return context.getRenovator().getFurniture(furnitureClass);
  }

  /**
   * Appends a collection of lines to the buffer, joining them with newline characters.
   *
   * @param lines the lines to append to the buffer
   */
  public void bufferAppendLines(final Collection<String> lines) {
    if (!lines.isEmpty()) {
      buffer.append(String.join("\n", lines));
    }
  }

  public void bufferAppendThrowable(final Throwable throwable) {
    bufferAppendLines(ErrorUtility.getStackTrace(throwable));
  }

  /**
   * Executes another command with specific arguments and the request context of the current
   * command.
   *
   * @param commandName the name of the command to execute
   * @param args the arguments to pass to the command
   * @param clearBuffer whether to clear the buffer after executing the command
   * @return the exit code of the executed command
   */
  public int executeOther(
      final String commandName, final List<String> args, final boolean clearBuffer) {
    final var exitCode = context.getProcessor().execute(commandName, args, requestContext);
    if (clearBuffer) {
      requestContext.getBuffer().setLength(0);
    }

    return exitCode;
  }

  /**
   * Executes another command with specific arguments and the request context of the current
   * command. Does not clear the buffer by default.
   *
   * @param commandName the name of the command to execute
   * @param args the arguments to pass to the command
   * @return the exit code of the executed command
   */
  public int executeOther(final String commandName, final List<String> args) {
    return executeOther(commandName, args, false);
  }

  @NonNull
  public static CommandLine.Command getCommandAnnotation(
      @NonNull final Class<? extends Command> commandClass) {
    return commandClass.getAnnotation(CommandLine.Command.class);
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
}
