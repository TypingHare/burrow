package me.jameschan.burrow.kernel.command.entry;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.entry.EntryNotFoundException;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "exist", description = "Check if an entry exists.")
@CommandType(CommandType.ENTRY)
public class ExistCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The id of the entry.")
  private Integer id;

  public ExistCommand(RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() {
    try {
      context.getHoard().getById(id);
      buffer.append(true);
    } catch (final EntryNotFoundException ex) {
      buffer.append(false);
    }

    return ExitCode.SUCCESS;
  }
}
