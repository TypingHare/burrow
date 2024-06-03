package me.jameschan.burrow.kernel.command.builtin;

import me.jameschan.burrow.kernel.ChamberInitializationException;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import me.jameschan.burrow.kernel.utility.ColorUtility;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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
  public Integer call() throws ChamberInitializationException {
    if (value == null) {
      retrieve();
      context.getChamber().restart();
    } else {
      updateConfigItem(context, key, value);
    }

    return ExitCode.SUCCESS;
  }

  private void retrieve() {
    final var value = retrieveConfigItem(context, key);
    if (value == null) {
      buffer.append(ColorUtility.render("null", ColorUtility.Type.NULL));
    } else {
      buffer.append(ColorUtility.render(value, ColorUtility.Type.VALUE));
    }
  }

  @Nullable
  public static String retrieveConfigItem(
      @NonNull final ChamberContext context, @NonNull final String key) {
    return context.getConfig().get(key);
  }

  public static void updateConfigItem(
      @NonNull final ChamberContext context,
      @NonNull final String key,
      @NonNull final String value) {
    context.getConfig().set(key, value);
  }
}
