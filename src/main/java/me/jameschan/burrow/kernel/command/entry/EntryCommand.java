package me.jameschan.burrow.kernel.command.entry;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "entry", description = "Find an entry by associated ID.")
public class EntryCommand extends Command {
  @CommandLine.Parameters(index = "0")
  private Integer id;

  public EntryCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() {
    final var hoard = context.getHoard();
    buffer.append(context.getFormatter().format(hoard.getById(id)));

    return ExitCode.SUCCESS;
  }
}
