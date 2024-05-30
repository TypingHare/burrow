package me.jameschan.burrow.kernel.command;

import java.util.Collection;
import java.util.concurrent.Callable;
import me.jameschan.burrow.kernel.ChamberModule;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.Furniture;
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

  protected final RequestContext requestContext;

  /** StringBuffer instance for buffering output or other data within the context. */
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

  @Override
  public int handleExecutionException(
      final Exception ex,
      final CommandLine commandLine,
      final CommandLine.ParseResult fullParseResult) {
    buffer.append("Fail to execute command: ").append(ex.getMessage());

    return ExitCode.ERROR;
  }

  /**
   * Retrieves an instance of Furniture by its class type using the renovator within the context.
   *
   * @param furnitureClass the class type of the Furniture to retrieve
   * @return an instance of the specified Furniture class
   */
  @NonNull
  public <T extends Furniture> T getFurniture(final Class<T> furnitureClass) {
    return context.getRenovator().getFurniture(furnitureClass);
  }

  public void bufferAppendLines(final Collection<String> lines) {
    if (!lines.isEmpty()) {
      buffer.append(String.join("\n", lines));
    }
  }
}
