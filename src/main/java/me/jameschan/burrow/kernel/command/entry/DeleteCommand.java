package me.jameschan.burrow.kernel.command.entry;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "delete", description = "Delete an entry.")
@CommandType(CommandType.ENTRY)
public class DeleteCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The ID of the entry to delete.")
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
