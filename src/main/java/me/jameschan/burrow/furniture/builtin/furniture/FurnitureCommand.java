package me.jameschan.burrow.furniture.builtin.furniture;

import java.util.Arrays;
import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.config.Config;
import me.jameschan.burrow.context.RequestContext;
import me.jameschan.burrow.furniture.Furniture;
import picocli.CommandLine;

@CommandLine.Command
public class FurnitureCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "instruction")
  private String instruction;

  @CommandLine.Parameters(
      index = "1",
      description = "The name of the furniture to add/delete.",
      defaultValue = "")
  private String furnitureName;

  public FurnitureCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws Exception {
    switch (instruction) {
      case INSTRUCTION.LIST -> printFurnitureNameList();
      case INSTRUCTION.ADD -> addFurniture();
      case INSTRUCTION.DELETE -> deleteFurniture();
      default -> buffer.append("No such instruction: ").append(instruction);
    }

    return 0;
  }

  private void printFurnitureNameList() {
    final var furnitureCollection = context.getRenovator().getAllFurniture();
    buffer.append("Furniture list:\n");
    var i = 0;
    for (final var furniture : furnitureCollection) {
      final var furnitureName = furniture.getClass().getName();
      buffer.append("[").append(i++).append("] ").append(furnitureName).append("\n");
    }
  }

  private void addFurniture() {
    // Checks if the furniture already exist
    final var renovator = context.getRenovator();
    final var config = context.getConfig();
    final var furnitureListString = config.get(Config.Key.FURNITURE_LIST);
    final var furnitureNameList =
        Arrays.stream(furnitureListString.split(":")).map(String::trim).toList();
    if (furnitureNameList.contains(furnitureName)) {
      buffer.append("Furniture already exists: ").append(furnitureName).append("\n");
      return;
    }

    // Checks if the furniture exists
    Class<? extends Furniture> clazz;
    try {
      clazz = renovator.checkIfFurnitureExist(furnitureName);
    } catch (final RuntimeException ex) {
      buffer.append("Furniture not found: ").append(furnitureName).append("\n");
      return;
    }

    // Checks if the furniture is valid
    try {
      renovator.testFurnitureClass(clazz);
    } catch (final ClassCastException ex) {
      buffer.append(ex.getMessage());
      return;
    }

    // Add furniture
    final var newFurnitureListString = furnitureListString + ":" + furnitureName;
    config.set(Config.Key.FURNITURE_LIST, newFurnitureListString);
    context.getChamber().saveConfig();
  }

  private void deleteFurniture() {
    final var config = context.getConfig();
    final var furnitureListString = config.get(Config.Key.FURNITURE_LIST);
    final var newFurnitureNameList =
        Arrays.stream(furnitureListString.split(":"))
            .map(String::trim)
            .filter(fn -> fn.equals(furnitureName))
            .toList();
    final var newFurnitureListString = String.join(":", newFurnitureNameList);
    config.set(Config.Key.FURNITURE_LIST, newFurnitureListString);
    context.getChamber().saveConfig();
  }

  public static final class INSTRUCTION {
    public static final String LIST = "list";
    public static final String ADD = "add";
    public static final String DELETE = "delete";
  }
}
