package me.jameschan.burrow.kernel.command.chamber;

import java.util.ArrayList;
import java.util.Arrays;
import me.jameschan.burrow.kernel.ChamberInitializationException;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.FurnitureNotFoundException;
import me.jameschan.burrow.kernel.furniture.InvalidFurnitureClassException;
import me.jameschan.burrow.kernel.furniture.Renovator;
import picocli.CommandLine;

@CommandLine.Command(
    name = "furniture",
    description = "List all furniture; add or delete a furniture.")
public class FurnitureCommand extends Command {
  @CommandLine.Parameters(
      index = "0",
      description = "instruction (list/add/delete)",
      defaultValue = "list")
  private String instruction;

  @CommandLine.Parameters(
      index = "1",
      paramLabel = "<furniture-name>",
      description = "The name of the furniture to add/delete.",
      defaultValue = "")
  private String furnitureName;

  public FurnitureCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws ChamberInitializationException {
    return switch (instruction) {
      case INSTRUCTION.LIST -> printFurnitureNameList();
      case INSTRUCTION.ADD -> addFurniture();
      case INSTRUCTION.REMOVE -> removeFurniture();
      case "" -> {
        buffer.append("Please specify an instruction: list/add/delete");
        yield ExitCode.ERROR;
      }
      default -> {
        buffer.append("No such instruction: ").append(instruction);
        yield ExitCode.ERROR;
      }
    };
  }

  private int printFurnitureNameList() {
    final var furnitureCollection = context.getRenovator().getAllFurniture();
    final var furnitureNameList = new ArrayList<String>();
    for (final var furniture : furnitureCollection) {
      final var furnitureName = furniture.getClass().getName();
      furnitureNameList.add("[" + furnitureNameList.size() + "] " + furnitureName);
    }

    buffer.append(String.join("\n", furnitureNameList));

    return ExitCode.SUCCESS;
  }

  private int addFurniture() throws ChamberInitializationException {
    // Checks if the furniture already exist
    final var renovator = context.getRenovator();
    final var config = context.getConfig();
    final var furnitureListString = config.get(Config.Key.FURNITURE_LIST);
    final var furnitureNameList =
        Arrays.stream(furnitureListString.split(Renovator.FURNITURE_NAME_SEPARATOR))
            .map(String::trim)
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

    // Add furniture
    final var newFurnitureListString = furnitureListString + ":" + furnitureName;
    config.set(Config.Key.FURNITURE_LIST, newFurnitureListString);
    context.getConfig().saveToFile();
    buffer.append("Furniture added: ").append(furnitureName);

    // Restart chamber
    context.getChamber().restart();

    return ExitCode.SUCCESS;
  }

  private int removeFurniture() {
    final var config = context.getConfig();
    final var furnitureListString = config.get(Config.Key.FURNITURE_LIST);
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

  public static final class INSTRUCTION {
    public static final String LIST = "list";
    public static final String ADD = "add";
    public static final String REMOVE = "delete";
  }
}
