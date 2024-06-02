package me.jameschan.burrow.kernel.command.builtin;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import me.jameschan.burrow.kernel.utility.ColorUtility;
import picocli.CommandLine;

@CommandLine.Command(
    name = "c",
    description = "Retrieve or update the value of a configuration item.")
@CommandType(CommandType.BUILTIN)
public class ConfigItemCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The key of the item to retrieve or update.")
  private String key;

  @CommandLine.Parameters(
      index = "1",
      description = "The value to be updated.",
      defaultValue = CommandLine.Option.NULL_VALUE)
  private String value;

  public ConfigItemCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    if (value == null) {
      retrieve();
    } else {
      update();
    }

    return ExitCode.SUCCESS;
  }

  private void retrieve() {
    final var config = context.getConfig();
    final var value = config.get(key);
    if (value == null) {
      buffer.append(ColorUtility.render("null", ColorUtility.Type.NULL));
    } else {
      buffer.append(ColorUtility.render(config.get(key), ColorUtility.Type.VALUE));
    }
  }

  private void update() {
    final var config = context.getConfig();
    config.set(key, value);
  }
}
