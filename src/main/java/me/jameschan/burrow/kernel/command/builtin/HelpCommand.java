package me.jameschan.burrow.kernel.command.builtin;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "help", description = "Show the usage of a command.")
@CommandType(CommandType.BUILTIN)
public class HelpCommand extends Command {
  @CommandLine.Parameters(
      index = "0",
      description = "The name of the command.",
      defaultValue = "help")
  private String commandName;

  public HelpCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() {
    final var command = context.getProcessor().getCommand(commandName);
    if (command == null) {
      buffer.append("No such command: ").append(commandName);
    } else {
      buffer.append(new CommandLine(command).getUsageMessage());
    }

    return ExitCode.SUCCESS;
  }
}
