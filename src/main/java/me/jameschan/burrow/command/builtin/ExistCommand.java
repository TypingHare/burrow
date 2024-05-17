package me.jameschan.burrow.command.builtin;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "exist", description = "Check if an entry exists.")
public class ExistCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The id of the entry.")
  private Integer id;

  public ExistCommand(RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() {
    final var hoard = context.getHoard();
    buffer.append(hoard.exist(id) ? "true" : "false");

    return 0;
  }
}
