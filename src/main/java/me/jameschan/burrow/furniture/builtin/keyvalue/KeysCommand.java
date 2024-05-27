package me.jameschan.burrow.furniture.builtin.keyvalue;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "keys", description = "Retrieve all keys.")
public class KeysCommand extends Command {
  public KeysCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws Exception {
    final var byKey = getFurniture(KeyValueFurniture.class).getKeyIdListMap();
    final var keyList = byKey.keySet().stream().sorted().toList();
    buffer.append(keyList);

    return 0;
  }
}
