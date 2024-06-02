package me.jameschan.burrow.kernel.command.entry;

import java.util.Map;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "set", description = "Set a property for a specific entry.")
@CommandType(CommandType.ENTRY)
public class SetCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The ID of the entry to update.")
  private Integer id;

  @CommandLine.Parameters(index = "1", description = "The key to set.")
  private String key;

  @CommandLine.Parameters(index = "2", description = "The value to set.")
  private String value;

  public SetCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    final var hoard = context.getHoard();
    final var entry = hoard.setProperties(id, Map.of(key, value));
    buffer.append(context.getFormatter().format(entry));

    return ExitCode.SUCCESS;
  }
}
