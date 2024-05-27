package me.jameschan.burrow.command;

import java.util.concurrent.Callable;
import me.jameschan.burrow.context.RequestContext;
import me.jameschan.burrow.furniture.Furniture;
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
public abstract class Command
    implements Callable<Integer>,
        CommandLine.IParameterExceptionHandler,
        CommandLine.IExecutionExceptionHandler {

  /** RequestContext instance holding the context in which this command is executed. */
  protected final RequestContext context;

  /** StringBuffer instance for buffering output or other data within the context. */
  protected final StringBuffer buffer;

  /**
   * Constructs a new Command with the given RequestContext. Initializes the buffer from the
   * context.
   *
   * @param context the RequestContext in which this command operates
   */
  public Command(final RequestContext context) {
    this.context = context;
    this.buffer = context.getBuffer();
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

    return 1;
  }

  @Override
  public int handleExecutionException(
      final Exception ex,
      final CommandLine commandLine,
      final CommandLine.ParseResult fullParseResult) {
    buffer.append("Fail to execute command: ").append(ex.getMessage());

    ex.printStackTrace();
    return 1;
  }

  /**
   * Retrieves an instance of Furniture by its class type using the renovator within the context.
   *
   * @param furnitureClass the class type of the Furniture to retrieve
   * @return an instance of the specified Furniture class
   */
  @NonNull
  public <T extends Furniture> T getFurniture(final Class<T> furnitureClass) {
    final var furniture = context.getRenovator().getFurniture(furnitureClass);
    if (furniture == null) {
      throw new RuntimeException("Failed to find furniture: " + furnitureClass.getName());
    }

    return furniture;
  }
}
