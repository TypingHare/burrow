package me.jameschan.burrow.furniture.keyvalue;

import java.util.ArrayList;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "values", description = "Retrieve values of a specified key.")
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
    final var idListStore = getFurniture(KeyValueFurniture.class).getIdListStore();
    final var idList = idListStore.getOrDefault(key, new ArrayList<>());
    final var hoard = context.getHoard();
    final var valueList =
        idList.stream().map(id -> hoard.getById(id).get(KeyValueFurniture.EntryKey.VALUE)).toList();

    buffer.append(context.getFormatter().format(valueList));

    return 0;
  }
}
