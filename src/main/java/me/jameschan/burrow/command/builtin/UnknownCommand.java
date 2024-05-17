package me.jameschan.burrow.command.builtin;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "unknown", description = "Unknown command.")
public class UnknownCommand extends Command {
  public UnknownCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() {
    return 0;
  }
}
