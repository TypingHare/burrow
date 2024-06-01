package me.jameschan.burrow.kernel.command.builtin;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Comparator;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.AmbiguousSimpleNameException;
import me.jameschan.burrow.kernel.furniture.FurnitureNotFoundException;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "list", description = "Print a list of all commands with descriptions.")
@CommandType(CommandType.BUILTIN)
public class CommandListCommand extends Command {
  @CommandLine.Parameters(
      index = "0",
      paramLabel = "<furniture-name>",
      description =
          "The name of a specific furniture. Only print the commands associated with the furniture.",
      defaultValue = "")
  private String furnitureName;

  public CommandListCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws FurnitureNotFoundException, AmbiguousSimpleNameException {
    final var commandClassList =
        furnitureName.isEmpty()
            ? context.getProcessor().getAllCommands()
            : context.getRenovator().getFurnitureByName(furnitureName).getAllCommands();
    final var sortedCommandClassList =
        commandClassList.stream().sorted(Comparator.comparing(Command::getName)).toList();

    final var lines = new ArrayList<String>();
    for (final var commandClass : sortedCommandClassList) {
      final var name = Command.getName(commandClass);
      final var descriptionArray = Command.getDescription(commandClass);
      final var description = descriptionArray.length > 0 ? descriptionArray[0] : "";
      lines.add(Strings.padEnd(name, 16, ' ') + description);
    }

    bufferAppendLines(lines);

    return ExitCode.SUCCESS;
  }
}
