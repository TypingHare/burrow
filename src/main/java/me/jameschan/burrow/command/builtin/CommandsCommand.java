package me.jameschan.burrow.command.builtin;

import com.google.common.base.Strings;
import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "commands", description = "List all commands and descriptions.")
public class CommandsCommand extends Command {
  public CommandsCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() {
    final var commands = context.getCommandManager().getAllCommands();
    commands.forEach(
        command -> {
          final var commandAnnotation = command.getDeclaredAnnotation(CommandLine.Command.class);
          final var name = commandAnnotation.name();
          final var descriptionArray = commandAnnotation.description();
          final var description = descriptionArray.length > 0 ? descriptionArray[0] : "";
          buffer.append(String.format("%s  %s%n", Strings.padEnd(name, 14, ' '), description));
        });

    return 0;
  }
}
