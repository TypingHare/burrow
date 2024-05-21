package me.jameschan.burrow.furniture.builtin.keyvalue;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "key", description = "Find an entry associated with a specified key.")
public class KeyCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The key of the entry.")
  private String key;

  public KeyCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws Exception {
    final var hoard = context.getHoard();
    final var idList = getFurniture(KeyValueFurniture.class).getIdListByKey(key);

    buffer.append("[\n");
    for (final var id : idList) {
      final var entry = hoard.getById(id);
      if (entry != null) {
        buffer.append(hoard.getFormattedEntryString(entry));
      }
    }
    buffer.append("]");

    return 0;
  }
}
