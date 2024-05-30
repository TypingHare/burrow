package me.jameschan.burrow.kernel.command.chamber;

import java.util.Arrays;
import me.jameschan.burrow.kernel.ChamberInitializationException;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.RequestContext;
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

    return ExitCode.SUCCESS;
  }

  private int addFurniture() throws ChamberInitializationException {

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
