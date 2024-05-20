package me.jameschan.burrow.command.builtin;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "delete", description = "Delete a command.")
public class DeleteCommand extends Command {
  @CommandLine.Parameters(index = "0")
  private Integer id;

  public DeleteCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() {
    final var hoard = context.getHoard();
    final var buffer = context.getBuffer();
    final var entry = hoard.delete(id);
    buffer.append(hoard.getFormattedEntryString(entry));

    return 0;
  }
}
