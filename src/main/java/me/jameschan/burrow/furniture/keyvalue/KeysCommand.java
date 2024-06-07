package me.jameschan.burrow.furniture.keyvalue;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "keys", description = "Retrieve all keys.")
@CommandType(CommandType.ENTRY)
public class KeysCommand extends Command {
  public KeysCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws Exception {
    final var idSetStore = getFurniture(KeyValueFurniture.class).getIdSetStore();
    final var keyList = idSetStore.keySet().stream().sorted().toList();
    buffer.append(context.getFormatter().format(keyList));

    return ExitCode.SUCCESS;
  }
}
