package me.jameschan.burrow.kernel.command.builtin;

import java.util.*;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.AmbiguousSimpleNameException;
import me.jameschan.burrow.kernel.furniture.FurnitureNotFoundException;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import me.jameschan.burrow.kernel.utility.ColorUtility;
import picocli.CommandLine;

@CommandLine.Command(name = "clist", description = "Display a list of all configuration items.")
@CommandType(CommandType.BUILTIN)
public class ConfigListCommand extends Command {
  public static final String KEY_VALUE_SEPARATOR = " -> ";

  @CommandLine.Option(
      names = {"-f", "--furniture"},
      description = "Filter configuration items by furniture name.",
      defaultValue = CommandLine.Option.NULL_VALUE)
  private String furnitureName;

  public ConfigListCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    final var configStore = context.getConfig().getStore();
    final var configKeys = furnitureName == null ? configStore.keySet() : getConfigKeys();
    final List<Map.Entry<String, String>> configToPrint =
        configStore.entrySet().stream()
            .filter(entry -> configKeys.contains(entry.getKey()))
            .sorted(Map.Entry.comparingByKey())
            .toList();

    final var lines = new ArrayList<String>();
    final var entries = configToPrint.stream().sorted(Map.Entry.comparingByKey()).toList();
    for (final var entry : entries) {
      lines.add(getColoredLine(entry.getKey(), entry.getValue()));
    }

    bufferAppendLines(lines);

    return ExitCode.SUCCESS;
  }

  private String getColoredLine(final String key, final String value) {
    return ColorUtility.render(key, ColorUtility.Type.KEY)
        + ColorUtility.render(KEY_VALUE_SEPARATOR, ColorUtility.Type.SYMBOL)
        + ColorUtility.render("\"" + value + "\"", ColorUtility.Type.VALUE);
  }

  private Collection<String> getConfigKeys()
      throws FurnitureNotFoundException, AmbiguousSimpleNameException {
    return Optional.ofNullable(
            context.getRenovator().getFurnitureByName(furnitureName).configKeys())
        .orElse(new ArrayList<>());
  }
}
