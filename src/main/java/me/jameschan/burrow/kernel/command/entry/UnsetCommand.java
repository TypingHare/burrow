package me.jameschan.burrow.kernel.command.entry;

import java.util.List;
import java.util.Objects;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.entry.Hoard;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "unset", description = "Unset a property for a specific entry.")
@CommandType(CommandType.ENTRY)
public class UnsetCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The ID of the entry to update.")
  private Integer id;

  @CommandLine.Parameters(index = "1", description = "The key to unset.")
  private String key;

  public UnsetCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    final var hoard = context.getHoard();
    final var entry = hoard.unsetProperties(id, List.of(key));

    if (Objects.equals(key, Hoard.KEY_ID)) {
      throw new RuntimeException("You cannot unset the ID property.");
    }

    buffer.append(context.getFormatter().format(entry));

    return ExitCode.SUCCESS;
  }
}
