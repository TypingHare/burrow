package me.jameschan.burrow.command.builtin;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "help", description = "Show the usage of a command.")
public class HelpCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The name of the command.", defaultValue = "")
  private String commandName;

  public HelpCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws Exception {
    final var commandManager = context.getCommandManager();
    final var commandName =
        this.commandName.isEmpty() ? context.getCommandName() : this.commandName;
    final var command = commandManager.getCommand(commandName);
    if (command == null) {
      buffer.append("No such command: ").append(commandName);
    } else {
      final var usage = new CommandLine(command).getUsageMessage();
      buffer.append(usage);
    }

    return 0;
  }
}
