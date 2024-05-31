package me.jameschan.burrow.kernel.command.chamber;

import com.google.common.base.Strings;
import java.util.ArrayList;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.AmbiguousSimpleNameException;
import me.jameschan.burrow.kernel.furniture.FurnitureNotFoundException;
import picocli.CommandLine;

@CommandLine.Command(
    name = "command-list",
    description = "Print a list of all commands with descriptions.")
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
    final var commands =
        furnitureName.isEmpty()
            ? context.getProcessor().getAllCommands()
            : context.getRenovator().getFurnitureByName(furnitureName).getAllCommands();

    final var commandStringLines = new ArrayList<String>();
    for (final var command : commands) {
      final var commandAnnotation = command.getDeclaredAnnotation(CommandLine.Command.class);
      final var name = commandAnnotation.name();
      final var descriptionArray = commandAnnotation.description();
      final var description = descriptionArray.length > 0 ? descriptionArray[0] : "";
      commandStringLines.add(Strings.padEnd(name, 16, ' ') + description);
    }

    if (!commandStringLines.isEmpty()) {
      buffer.append(String.join("\n", commandStringLines));
    }

    return ExitCode.SUCCESS;
  }
}
