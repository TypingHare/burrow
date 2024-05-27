package me.jameschan.burrow.command.builtin;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "unknown", description = "Unknown command.")
public class UnknownCommand extends Command {
  @CommandLine.Parameters(arity = "0..*")
  private String[] args;

  public UnknownCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() {
    final var commandName = context.getCommandName();
    buffer.append("Unknown command: ").append(commandName);
    if (args != null && args.length > 0) {
      buffer.append("\n").append("Arguments: ").append(String.join(" ", args));
    }

    return 0;
  }
}
