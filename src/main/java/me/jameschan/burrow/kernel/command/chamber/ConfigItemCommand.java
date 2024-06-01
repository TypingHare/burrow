package me.jameschan.burrow.kernel.command.chamber;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.utility.ColorUtility;
import picocli.CommandLine;

@CommandLine.Command(name = "c", description = "Retrieve the value of a config item.")
public class ConfigItemCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The key of the item to retrieve.")
  private String key;

  public ConfigItemCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    final var config = context.getConfig();
    final var value = config.get(key);
    if (value == null) {
      buffer.append(ColorUtility.render("null", ColorUtility.Type.NULL));
    } else {
      buffer.append(ColorUtility.render(config.get(key), ColorUtility.Type.VALUE));
    }

    return ExitCode.SUCCESS;
  }
}
