package me.jameschan.burrow.command.builtin;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "entry", description = "Find an entry by associated ID.")
public class EntryCommand extends Command {
  @CommandLine.Parameters(index = "0")
  private Integer id;

  public EntryCommand(RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() {
    final var hoard = context.getHoard();
    final var entry = hoard.getById(id);
    if (entry == null) {
      buffer.append("Could not find entry with ID ").append(id);
    } else {
      buffer.append(hoard.getFormattedEntryString(entry));
    }

    return 0;
  }
}
