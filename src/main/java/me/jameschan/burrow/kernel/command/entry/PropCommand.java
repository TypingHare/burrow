package me.jameschan.burrow.kernel.command.entry;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import me.jameschan.burrow.kernel.utility.ColorUtility;
import picocli.CommandLine;

@CommandLine.Command(name = "prop", description = "Retrieve the property of a specific entry.")
@CommandType(CommandType.ENTRY)
public class PropCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The ID of the entry to retrieve.")
  private Integer id;

  @CommandLine.Parameters(index = "1", description = "The key to retrieve.")
  private String key;

  public PropCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    final var entry = context.getHoard().getById(id);
    final var value = entry.get(key);

    if (value == null) {
      buffer.append(ColorUtility.render("null", ColorUtility.Type.NULL));
    } else {
      buffer.append(ColorUtility.render(value, ColorUtility.Type.VALUE));
    }

    return ExitCode.SUCCESS;
  }
}
