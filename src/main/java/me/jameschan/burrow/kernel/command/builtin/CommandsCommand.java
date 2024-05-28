package me.jameschan.burrow.kernel.command.builtin;

import com.google.common.base.Strings;
import java.util.ArrayList;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "commands", description = "List all commands with descriptions.")
public class CommandsCommand extends Command {
  public CommandsCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    final var commands = context.getProcessor().getAllCommands();
    final var commandStringLines = new ArrayList<String>();
    for (final var command : commands) {
      final var commandAnnotation = command.getDeclaredAnnotation(CommandLine.Command.class);
      final var name = commandAnnotation.name();
      final var descriptionArray = commandAnnotation.description();
      final var description = descriptionArray.length > 0 ? descriptionArray[0] : "";
      commandStringLines.add(String.format("%s: %s", Strings.padEnd(name, 14, ' '), description));
    }

    buffer.append(String.join("\n", commandStringLines));

    return 0;
  }
}
