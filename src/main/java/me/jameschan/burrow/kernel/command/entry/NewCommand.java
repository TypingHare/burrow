package me.jameschan.burrow.kernel.command.entry;

import java.util.Map;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(
    name = "new",
    mixinStandardHelpOptions = true,
    description = "Create a new entry and print the new try.")
public class NewCommand extends Command {
  public NewCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() {
    final var entry = context.getHoard().create(Map.of());
    buffer.append(context.getFormatter().format(entry));

    return ExitCode.SUCCESS;
  }
}
