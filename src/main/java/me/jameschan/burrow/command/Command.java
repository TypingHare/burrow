package me.jameschan.burrow.command;

import java.util.concurrent.Callable;
import me.jameschan.burrow.context.RequestContext;
import me.jameschan.burrow.furniture.Furniture;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Abstract class representing a command that can be executed within a given request context. This
 * class implements Callable with a return type of Integer, allowing it to be executed by a thread
 * or executor.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class Command implements Callable<Integer> {

  // RequestContext instance holding the context in which this command is executed.
  protected final RequestContext context;

  // StringBuffer instance for buffering output or other data within the context.
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

  /**
   * Retrieves an instance of Furniture by its class type using the renovator within the context.
   *
   * @param furnitureClass the class type of the Furniture to retrieve
   * @return an instance of the specified Furniture class
   */
  public <T extends Furniture> T getFurniture(final Class<T> furnitureClass) {
    return context.getRenovator().getFurniture(furnitureClass);
  }
}
