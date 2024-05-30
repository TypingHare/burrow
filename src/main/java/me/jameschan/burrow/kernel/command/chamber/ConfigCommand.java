package me.jameschan.burrow.kernel.command.chamber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.jameschan.burrow.kernel.ChamberInitializationException;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.IllegalKeyException;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.AmbiguousSimpleNameException;
import me.jameschan.burrow.kernel.furniture.FurnitureNotFoundException;
import picocli.CommandLine;

@CommandLine.Command(
    name = "config",
    description =
        "Retrieve or update configuration settings. "
            + "If no parameters are provided, the entire configuration is displayed. "
            + "If only a key is provided, the value for that key is retrieved. "
            + "If both key and value are provided, the configuration is updated with the new value.")
public class ConfigCommand extends Command {
  public static final String KEY_VALUE_SEPARATOR = " -> ";

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

  @CommandLine.Option(
      names = {"-f", "--furniture"},
      description = "",
      defaultValue = "")
  private String furnitureName;

  public ConfigCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call()
      throws IllegalKeyException,
          FurnitureNotFoundException,
          AmbiguousSimpleNameException,
          ChamberInitializationException {
    if (value.isEmpty()) {
      if (key.isEmpty()) {
        printAllConfig();
      } else {
        printSpecificConfig();
      }
    } else {
      updateConfig();
    }

    return ExitCode.SUCCESS;
  }

  private void printAllConfig() throws FurnitureNotFoundException, AmbiguousSimpleNameException {
    final var configStore = context.getConfig().getStore();
    final var furniture =
        furnitureName.isEmpty() ? null : context.getRenovator().getFurnitureByName(furnitureName);
    final var configKeys = furniture == null ? null : furniture.configKeys();
    final List<Map.Entry<String, String>> printedConfig =
        (furniture == null)
            ? configStore.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()
            : configStore.entrySet().stream()
                .filter(entry -> configKeys != null && configKeys.contains(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .toList();

    final var lines = new ArrayList<String>();
    final var entries = printedConfig.stream().sorted(Map.Entry.comparingByKey()).toList();
    for (final var entry : entries) {
      lines.add(getColoredLine(entry.getKey(), entry.getValue()));
    }

    bufferAppendLines(lines);
  }

  private void printSpecificConfig() {
    final var config = context.getConfig();
    buffer.append(getColoredLine(key, config.get(key)));
  }

  private void updateConfig() throws ChamberInitializationException {
    final var config = context.getConfig();

    config.set(key, value);
    context.getConfig().saveToFile();
    try {
      context.getChamber().restart();
    } catch (final ChamberInitializationException ex) {
      buffer.append("Fail to update config due to a failure of restart.");

      throw ex;
    }

    buffer.append(getColoredLine(key, value));
  }

  private String getColoredKey(final String key) {
    return CommandLine.Help.Ansi.ON.string("@|blue " + key + "|@");
  }

  private String getColoredValue(final String value) {
    return CommandLine.Help.Ansi.ON.string("@|green " + value + "|@");
  }

  private String getColoredLine(final String key, final String value) {
    return getColoredKey(key)
        + CommandLine.Help.Ansi.ON.string("@|magenta " + KEY_VALUE_SEPARATOR + "|@")
        + getColoredValue("\"" + value + "\"");
  }
}
