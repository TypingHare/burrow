package me.jameschan.burrow.furniture.builtin.keyvalue;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(
    name = "count",
    description = "Get the count of a entries associated with a specified key.")
public class CountCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The key.")
  private String key;

  /**
   * Constructs a new Command with the given RequestContext. Initializes the buffer from the
   * context.
   *
   * @param context the RequestContext in which this command operates
   */
  public CountCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws Exception {
    final var count = getFurniture(KeyValueFurniture.class).getIdListByKey(key).size();
    buffer.append(count);

    return 0;
  }
}
