package me.jameschan.burrow.kernel.command.builtin;

import java.util.Arrays;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.Renovator;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "frm", description = "Remove a furniture from the current chamber.")
@CommandType(CommandType.BUILTIN)
public class FurnitureRemoveCommand extends Command {
  @CommandLine.Parameters(
      index = "0",
      paramLabel = "<furniture-name>",
      description = "The full name of the furniture to removes.")
  private String furnitureName;

  public FurnitureRemoveCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    final var config = context.getConfig();
    final var furnitureListString = config.getOrDefault(Config.Key.FURNITURE_LIST, "");
    assert furnitureListString != null;
    final var newFurnitureNameList =
        Arrays.stream(furnitureListString.split(Renovator.FURNITURE_NAME_SEPARATOR))
            .map(String::trim)
            .filter(fn -> fn.equals(furnitureName))
            .toList();
    final var newFurnitureListString =
        String.join(Renovator.FURNITURE_NAME_SEPARATOR, newFurnitureNameList);
    config.set(Config.Key.FURNITURE_LIST, newFurnitureListString);
    context.getConfig().saveToFile();

    return ExitCode.SUCCESS;
  }
}
