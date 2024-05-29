package me.jameschan.burrow.kernel.command.entry;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
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
    final var entry = context.getHoard().delete(id);
    buffer.append(context.getFormatter().format(entry));

    return ExitCode.SUCCESS;
  }
}
