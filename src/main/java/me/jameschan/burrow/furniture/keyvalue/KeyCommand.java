package me.jameschan.burrow.furniture.keyvalue;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "key", description = "Find an entry associated with a specified key.")
@CommandType(CommandType.ENTRY)
public class KeyCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The key of the entry.")
  private String key;

  public KeyCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws Exception {
    final var hoard = context.getHoard();
    final var idList = getFurniture(KeyValueFurniture.class).getIdSetByKey(key);
    final var valueList =
        idList.stream()
            .map(hoard::getById)
            .map(entry -> entry.get(KeyValueFurniture.EntryKey.VALUE))
            .toList();

    buffer.append(context.getFormatter().format(valueList));

    return ExitCode.SUCCESS;
  }
}
