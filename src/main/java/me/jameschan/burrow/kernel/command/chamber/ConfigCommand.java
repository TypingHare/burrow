package me.jameschan.burrow.kernel.command.chamber;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(
    name = "config",
    description =
        "Retrieve or update configuration settings. "
            + "If no parameters are provided, the entire configuration is displayed. "
            + "If only a key is provided, the value for that key is retrieved. "
            + "If both key and value are provided, the configuration is updated with the new value.")
public class ConfigCommand extends Command {

  @CommandLine.Parameters(
      index = "0",
      description =
          "The configuration key to retrieve or update. "
              + "If no key is provided, all configuration settings are displayed.",
      defaultValue = "")
  private String key;

  @CommandLine.Parameters(
      index = "1",
      description =
          "The new value to set for the configuration key. "
              + "If no value is provided, the current value of the key is displayed.",
      defaultValue = "")
  private String value;

  public ConfigCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws Exception {
    final var config = context.getConfig();

    if (value.isEmpty()) {
      if (key.isEmpty()) {
        buffer.append(context.getConfig().getStore());
      } else {
        // Retrieve configuration
        buffer.append(String.format("%s -> \"%s\"", key, config.get(key)));
      }
    } else {
      // Update configuration
      config.set(key, value);
      buffer.append(String.format("%s -> \"%s\"", key, value));
      context.getConfig().saveToFile();
      context.getChamber().restart();
    }

    return 0;
  }
}
