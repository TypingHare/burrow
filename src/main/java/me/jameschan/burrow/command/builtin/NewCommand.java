package me.jameschan.burrow.command.builtin;

import java.util.Map;
import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(
    name = "new",
    mixinStandardHelpOptions = true,
    description = "Create a new entry.")
public class NewCommand extends Command {
  public NewCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() {
    final var hoard = context.getHoard();
    final var entry = hoard.create(Map.of());
    buffer.append(hoard.getFormattedEntryString(entry));

    return 0;
  }
}
