package me.jameschan.burrow.furniture.keyvalue;

import java.util.HashMap;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "new", description = "Create an entry with key and value.")
@CommandType(CommandType.ENTRY)
public class NewCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The key of the entry.")
  private String key;

  @CommandLine.Parameters(index = "1", description = "The value of the entry.")
  private String value;

  public NewCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws Exception {
    final var hoard = context.getHoard();
    final var properties = new HashMap<String, String>();
    properties.put(KeyValueFurniture.EntryKey.KEY, key);
    properties.put(KeyValueFurniture.EntryKey.VALUE, value);
    final var entry = hoard.create(properties);
    buffer.append(context.getFormatter().format(entry));

    return ExitCode.SUCCESS;
  }
}
