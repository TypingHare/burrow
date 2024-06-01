package me.jameschan.burrow.kernel.command.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;
import me.jameschan.burrow.kernel.ChamberInitializationException;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.FurnitureNotFoundException;
import me.jameschan.burrow.kernel.furniture.InvalidFurnitureClassException;
import me.jameschan.burrow.kernel.furniture.Renovator;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "fadd", description = "Add a furniture to the chamber.")
@CommandType(CommandType.BUILTIN)
public class FurnitureAddCommand extends Command {
  @CommandLine.Parameters(
      index = "0",
      paramLabel = "<furniture-name>",
      description = "The full name of the furniture to add/delete.")
  private String furnitureName;

  public FurnitureAddCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    // Checks if the furniture already exist
    final var renovator = context.getRenovator();
    final var config = context.getConfig();
    final var furnitureListString = config.get(Config.Key.FURNITURE_LIST);
    assert furnitureListString != null;
    final var furnitureNameList =
        Arrays.stream(furnitureListString.split(Renovator.FURNITURE_NAME_SEPARATOR))
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .toList();
    if (furnitureNameList.contains(furnitureName)) {
      buffer.append("Furniture already exists: ").append(furnitureName);
      return ExitCode.ERROR;
    }

    // Checks if the furniture exists and is valid
    try {
      final var furnitureClass = renovator.checkIfFurnitureExist(furnitureName);
      renovator.testFurnitureClass(furnitureClass);
    } catch (final FurnitureNotFoundException | InvalidFurnitureClassException ex) {
      buffer.append(ex.getMessage());
      return ExitCode.ERROR;
    }

    // Add the furniture to the list and update the config file
    final var newFurnitureList = new ArrayList<>(furnitureNameList);
    newFurnitureList.add(furnitureName);
    final var newFurnitureListString =
        String.join(Renovator.FURNITURE_NAME_SEPARATOR, newFurnitureList);
    config.set(Config.Key.FURNITURE_LIST, newFurnitureListString);
    context.getConfig().saveToFile();

    // Restart chamber
    try {
      context.getChamber().restart();
    } catch (final ChamberInitializationException ex) {
      // Restore to the former furniture list and update the config file
      config.set(Config.Key.FURNITURE_LIST, furnitureListString);
      context.getConfig().saveToFile();
      buffer
          .append("Fail to add the furniture due to a failure of restarting.\n")
          .append("Error: ")
          .append(ex.getCause().getMessage());
      return ExitCode.ERROR;
    }

    return ExitCode.SUCCESS;
  }
}
