package me.jameschan.burrow.furniture.keyvalue;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "values", description = "Retrieve values of a specified key.")
@CommandType(CommandType.ENTRY)
public class ValuesCommand extends Command {
  @CommandLine.Parameters(
      index = "0",
      description = "The associated key of the values to retrieve.")
  private String key;

  public ValuesCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws Exception {
    final var idSet = getFurniture(KeyValueFurniture.class).getIdSetByKey(key);
    final var hoard = context.getHoard();
    final var valueList =
        idSet.stream().map(id -> hoard.getById(id).get(KeyValueFurniture.EntryKey.VALUE)).toList();

    buffer.append(context.getFormatter().format(valueList));

    return 0;
  }
}
