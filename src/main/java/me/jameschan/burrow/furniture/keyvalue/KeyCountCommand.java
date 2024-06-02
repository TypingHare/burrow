package me.jameschan.burrow.furniture.keyvalue;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(
    name = "key-count",
    description = "Get the count of entries associated with a specified key.")
@CommandType(CommandType.ENTRY)
public class KeyCountCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The key.")
  private String key;

  /**
   * Constructs a new Command with the given RequestContext. Initializes the buffer from the
   * context.
   *
   * @param context the RequestContext in which this command operates
   */
  public KeyCountCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws Exception {
    final var count = getFurniture(KeyValueFurniture.class).getIdListByKey(key).size();
    buffer.append(count);

    return ExitCode.SUCCESS;
  }
}
